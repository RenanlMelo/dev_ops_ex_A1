import groovy.json.JsonSlurperClassic

// Pipeline B1: one deployable modular Core, existing frontend and observability.
// Jenkins Secret Text IDs are documented in docs/arquitetura/README.md.
def runCmd(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat "@echo off\r\n${command}"
    }
}

def outputCmd(String command) {
    if (isUnix()) {
        return sh(script: "set +x\n${command}", returnStdout: true).trim()
    }
    return bat(script: "@echo off\r\n${command}", returnStdout: true).trim()
}

def maven(String goals) {
    if (isUnix()) {
        sh "cd backend && ./mvnw -B ${goals}"
    } else {
        bat "@echo off\r\ncd backend && mvnw.cmd -B ${goals}"
    }
}

pipeline {
    agent any
    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timeout(time: 40, unit: 'MINUTES')
    }
    environment {
        POSTGRES_PASSWORD = credentials('b1-postgres-password')
        PGADMIN_DEFAULT_PASSWORD = credentials('b1-pgadmin-password')
        GRAFANA_ADMIN_PASSWORD = credentials('b1-grafana-admin-password')
        GRAFANA_ADMIN_USER = 'admin'
    }
    stages {
        stage('1 - Checkout') {
            steps {
                checkout scm
                script {
                    runCmd('java -version')
                    runCmd('docker compose version')
                }
            }
        }
        stage('2 - CI - Build + Testes + JaCoCo') {
            steps { script { maven('clean verify') } }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'backend/target/site/jacoco/**', allowEmptyArchive: true
                }
                success {
                    recordCoverage tools: [[parser: 'JACOCO', pattern: 'backend/target/site/jacoco/jacoco.xml']]
                }
            }
        }
        stage('3 - CI - Qualidade PMD') {
            steps { script { maven('pmd:check') } }
            post {
                always {
                    archiveArtifacts artifacts: 'backend/target/pmd.xml', allowEmptyArchive: true
                }
            }
        }
        stage('4 - CI - Package') {
            steps { script { maven('package -DskipTests') } }
            post {
                success { archiveArtifacts artifacts: 'backend/target/*.jar', fingerprint: true }
            }
        }
        stage('5 - CD - Build dos Containers') {
            steps { script { runCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml build app frontend') } }
        }
        stage('6 - CD - Deploy HOMOL') {
            steps {
                script {
                    runCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml up -d app frontend postgres pgadmin prometheus grafana')
                    runCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml restart grafana')
                }
            }
        }
        stage('7 - CD - Health Check da API') {
            steps {
                script {
                    try {
                        if (isUnix()) {
                            sh 'for i in $(seq 1 18); do docker compose -f docker-compose.yml -f docker-compose.homol.yml exec -T app curl -sf http://localhost:8080/actuator/health && exit 0; sleep 5; done; exit 1'
                        } else {
                            bat '''@echo off
                            setlocal enabledelayedexpansion
                            set n=0
                            :healthloop
                            set /a n+=1
                            docker compose -f docker-compose.yml -f docker-compose.homol.yml exec -T app curl -sf http://localhost:8080/actuator/health
                            if !errorlevel! EQU 0 goto healthok
                            if !n! GEQ 18 exit /b 1
                            powershell -NoProfile -Command "Start-Sleep -Seconds 5"
                            goto healthloop
                            :healthok
                            exit /b 0
                            '''
                        }
                    } catch (err) {
                        runCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml ps')
                        runCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml logs --tail=100 app')
                        throw err
                    }
                }
            }
        }
        stage('7b - CD - Integridade relacional') {
            steps { script { runCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml exec -T postgres psql -U devopsac1 -d devopsac1 -v ON_ERROR_STOP=1 < docs/arquitetura/sql/001-ensure-enrollment-fks.sql') } }
        }
        stage('8 - CD - Cypress E2E') {
            steps { script { runCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml --profile e2e run --rm cypress') } }
            post {
                always {
                    archiveArtifacts artifacts: 'frontend/cypress/screenshots/**,frontend/cypress/videos/**', allowEmptyArchive: true
                }
            }
        }
        stage('9 - Observabilidade') {
            steps {
                script {
                    retry(12) {
                        sleep time: 5, unit: 'SECONDS'
                        def raw = outputCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml exec -T app curl -sf http://prometheus:9090/api/v1/targets')
                        def targets = new JsonSlurperClassic().parseText(raw)
                        def match = targets.data.activeTargets.find { it.labels.job == 'jogo-enigma-api' && it.labels.instance == 'app:8080' }
                        if (targets.status != 'success' || match == null || match.health != 'up') {
                            error('Prometheus ainda nao coleta app:8080 com estado UP')
                        }
                    }
                    def queryRaw = outputCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml exec -T app curl -sf http://prometheus:9090/api/v1/query?query=up')
                    def query = new JsonSlurperClassic().parseText(queryRaw)
                    def series = query.data.result.find { it.metric.job == 'jogo-enigma-api' && it.metric.instance == 'app:8080' }
                    if (query.status != 'success' || series == null || series.value[1] != '1') {
                        error('Prometheus up{job="jogo-enigma-api"} nao retornou 1 para app:8080')
                    }
                    String auth = isUnix() ? '-u "$GRAFANA_ADMIN_USER:$GRAFANA_ADMIN_PASSWORD"' : '-u "%GRAFANA_ADMIN_USER%:%GRAFANA_ADMIN_PASSWORD%"'
                    def datasources = new JsonSlurperClassic().parseText(outputCmd("curl -sf ${auth} http://localhost:3002/api/datasources"))
                    def prometheus = datasources.find { it.name == 'Prometheus' && it.type == 'prometheus' }
                    if (prometheus == null) { error('Datasource Prometheus nao encontrado no Grafana') }
                    def proxy = new JsonSlurperClassic().parseText(outputCmd("curl -sf ${auth} http://localhost:3002/api/datasources/proxy/uid/${prometheus.uid}/api/v1/query?query=up"))
                    def grafanaSeries = proxy.data.result.find { it.metric.job == 'jogo-enigma-api' && it.metric.instance == 'app:8080' }
                    if (proxy.status != 'success' || grafanaSeries == null || grafanaSeries.value[1] != '1') {
                        error('Grafana nao conseguiu consultar o Prometheus para app:8080')
                    }
                    def dashboard = new JsonSlurperClassic().parseText(outputCmd("curl -sf ${auth} http://localhost:3002/api/dashboards/uid/a1-observabilidade-homol"))
                    if (dashboard.dashboard?.uid != 'a1-observabilidade-homol') {
                        error('Dashboard A1 de observabilidade nao encontrado no Grafana')
                    }
                    echo 'Prometheus target UP, consulta up=1, datasource e dashboard Grafana operacionais.'
                }
            }
        }
    }
    post {
        success { echo 'Pipeline B1: testes, fronteiras modulares, PMD, deploy, E2E e observabilidade validados.' }
        failure { echo 'Pipeline falhou: confira o stage e o Console Output.' }
    }
}
