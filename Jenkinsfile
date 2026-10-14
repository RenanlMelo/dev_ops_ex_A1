// Pipeline as Code - CI (build, testes, JaCoCo), CD (containers, health check)
// e Observabilidade (Prometheus + Grafana) para o dev_ops_ac1.

def runCmd(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}

pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('1 - Checkout') {
            steps { checkout scm }
        }

        stage('2 - CI - Build + Testes + JaCoCo') {
            steps { script { runCmd('mvn -f backend/pom.xml clean verify') } }
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
            steps { script { runCmd('mvn -B -f backend/pom.xml pmd:pmd -DskipTests') } }
            post {
                always {
                    archiveArtifacts artifacts: 'backend/target/pmd.xml,backend/target/reports/**', allowEmptyArchive: true
                }
            }
        }

        stage('4 - CI - Package') {
            steps { script { runCmd('mvn -f backend/pom.xml package -DskipTests') } }
        }

        stage('5 - CD - Build dos Containers') {
            steps { script { runCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml build app frontend') } }
        }

        stage('6 - CD - Deploy HOMOL') {
            steps {
                script {
                    runCmd('docker compose -f docker-compose.yml -f docker-compose.homol.yml up -d app frontend postgres pgadmin prometheus grafana')
                    // Recarrega os providers de dashboards adicionados ao volume de provisioning.
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
                            bat '''
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
                echo 'API (health):  http://localhost:8080/actuator/health'
                echo 'Frontend:      http://localhost:8082'
                echo 'Prometheus:    http://localhost:9090/targets'
                echo 'Grafana:       http://localhost:3002'
                echo 'Portas acima sao os defaults do Compose; confira overrides no .env do agente.'
                echo 'Consulta Grafana: up{job="jogo-enigma-api"} (nome do roteiro, target real app:8080 do A1)'
                echo 'Este stage informa os enderecos; valide o target UP e a consulta com resultado 1.'
            }
        }
    }

    post {
        success { echo 'Pipeline concluido: testes, cobertura, relatorio PMD, containers, health check e E2E ok. Confira as evidencias de observabilidade.' }
        failure { echo 'Pipeline falhou - veja o stage e os logs para identificar a causa.' }
    }
}
