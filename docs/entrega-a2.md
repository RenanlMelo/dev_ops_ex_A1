# Esteira A2 aplicada ao projeto A1

O job Freestyle `A2_P1_integracao_ci` e o job Pipeline `pipeline_cypress_v3`
utilizam o repositorio da equipe, `dev_ops_ex_A1`, na branch `main`.
O Jogo Enigma serve como referencia; a aplicacao entregue e a do A1.

## Configuracao dos jobs

- Freestyle: POM `backend/pom.xml`, goals `clean verify` (ou `clean install`),
  JUnit `backend/target/surefire-reports/*.xml` e cobertura JaCoCo
  `backend/target/site/jacoco/jacoco.xml`.
- Pipeline: `Pipeline script from SCM`, Git, branch `*/main`, Script Path
  `Jenkinsfile`. Requer Java 21 ou superior, Maven, Docker Compose e os plugins
  Pipeline, Git, JUnit e Coverage (passo `recordCoverage`).
- As alteracoes locais precisam estar commitadas e enviadas ao Git utilizado
  pelo job antes de executar a nova versao no Jenkins.

## Fluxo

Git -> Checkout -> CI (JUnit + JaCoCo -> PMD -> Package) ->
CD (Docker Build -> Deploy HOMOL -> Health Check -> Cypress) ->
verificacao automatica de Prometheus e Grafana no pipeline B1.

O PMD era informativo na esteira A2; no pipeline B1, `pmd:check` bloqueia o deploy
se houver violacoes. Falhas de execucao Maven e testes tambem bloqueiam os
estagios seguintes. Os relatorios PMD e JaCoCo ficam nos artefatos do build.

O deploy combina `docker-compose.yml` com `docker-compose.homol.yml`.
O complemento conecta a API ao PostgreSQL e aguarda o banco ficar saudavel.
O Compose base continua disponivel para desenvolvimento com H2.
Os dados existentes em H2 nao sao migrados automaticamente para PostgreSQL.
Os volumes existentes nao sao removidos pelo pipeline.

## Enderecos padrao do A1

| Componente | Endereco |
| --- | --- |
| API | http://localhost:8080/actuator/health |
| Frontend Vue | http://localhost:8082 |
| Prometheus | http://localhost:9090/targets |
| Grafana | http://localhost:3002 |

Variaveis locais podem sobrescrever as portas. O Jenkins observado no ambiente
da equipe utiliza http://localhost:8180.
O frontend do A1 utiliza sua propria arquitetura; nao foi adicionado o BFF do
Jogo Enigma.

O nome de coleta `jogo-enigma-api` foi mantido para compatibilidade com a
consulta do roteiro, mas o endereco coletado e `app:8080`, a API do A1.
No Grafana, use `up{job="jogo-enigma-api"}` e confirme o valor 1.
O stage Observabilidade do pipeline B1 valida target UP no Prometheus e consulta
`up=1` diretamente no Prometheus e atraves do datasource do Grafana.

## Evidencias da entrega

1. Freestyle: configuracao, falha de teste, correcao, JUnit e JaCoCo.
2. Pipeline: Stage View do A1 e explicacao de Pipeline as Code.
3. Containers do A1, health UP na porta 8080, Cypress aprovado, target UP no
   Prometheus da porta 9090 e consulta com valor 1 no Grafana da porta 3002.
4. Desenho do fluxo distinguindo CI, CD e observabilidade.
5. Conclusao sobre a automacao e a entrega incorporadas na A2.

Os prints da API na porta 8081 e do Grafana na porta 3001 pertencem ao ambiente
Jogo Enigma e devem ser substituidos por evidencias do A1 para comprovar a
adaptacao. Entregar o link do repositorio e as evidencias em um unico PDF.
