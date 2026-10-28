# Evidências de validação — B1 Desafio 1

Execução local em 27/10/2026, branch `codex/b1-desafio1-core`. A referência definitiva da revisão é o hash do commit entregue.

| Verificação | Comando / método | Resultado |
|---|---|---|
| Suíte e qualidade | `cd backend; ./mvnw.cmd -B clean verify pmd:check` com JDK 21 | 42 testes, 0 falhas, 0 erros, 0 ignorados; PMD 0 violações; `BUILD SUCCESS`. JaCoCo: 545/562 instruções e 6/6 branches (contagem global do relatório). |
| Imagem e seis serviços | `docker compose -f docker-compose.yml -f docker-compose.homol.yml -f <override-isolado> up -d --build app frontend postgres pgadmin prometheus grafana` | Build concluído; app e PostgreSQL saudáveis, frontend, pgAdmin, Prometheus e Grafana ativos em projeto Compose separado. |
| Fluxo HTTP + PostgreSQL | `POST /students`, `/courses`, `/enrollments`, `/enrollments/{id}/complete` com nota 8,5; `GET /students/{id}/eligible-for-extra-courses`; consulta SQL | Conclusão e elegibilidade verdadeiras; linha persistida no PostgreSQL. Referência de curso inexistente retornou 404. |
| Compatibilidade do esquema | App nova com `ddl-auto=validate` contra banco A1 existente; script `001-ensure-enrollment-fks.sql` no banco isolado | Inicialização e health `UP` com esquema anterior; duas FKs preservadas/criadas sem perda de linhas. |
| E2E do frontend real | `docker compose ... --profile e2e run --rm cypress` | 2 cenários aprovados após rebuild do frontend. O Nginx encaminhou `/api` para a API real; não há stub no teste. |
| Observabilidade | API Prometheus `/api/v1/targets` e `/api/v1/query?query=up`; API Grafana datasource proxy e dashboard | `app:8080` no job `jogo-enigma-api` com alvo `UP`, série `up=1`; Grafana consultou a mesma série e encontrou `a1-observabilidade-homol`. |

O override isolado removeu a publicação de portas e usou o nome `dev_ops_ac1_b1_smoke`, sem substituir o Compose A1 já em operação. O teste foi removido ao final; o ambiente A1 anterior e o Jogo Enigma permaneceram preservados.

**Pendente:** a execução do `Jenkinsfile` revisado no job `pipeline_cypress_v3` não foi validada nesta sessão. O acesso automatizado ao Jenkins pelo navegador foi recusado pela política de segurança da sessão; portanto não há número de build B1 a citar. O job precisa das três credenciais Secret Text documentadas no [README.md](README.md), apontar para o commit desta entrega e ser executado por um usuário autorizado. Não assumir aprovação do Jenkins a partir dos testes locais.
