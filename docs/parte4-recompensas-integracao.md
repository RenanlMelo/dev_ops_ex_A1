# Parte 4 — Recompensas e integração final

Implementação adaptada do ZIP fornecido, sobre a parte 3. O ranking e seus testes da parte 2 foram preservados.

## Branches e PRs

1. `codex/b1-parte3-conquistas-moedas` → base `codex/b1-parte2-ranking`.
2. `codex/b1-parte4-recompensas-integracao` → base `codex/b1-parte3-conquistas-moedas`.

A segunda branch contém a integração completa. Fazer merge na ordem acima; depois, integrar a cadeia na branch de entrega do grupo.

## Funcionalidades

- Catálogo: `GET /rewards`.
- Resgate: `POST /students/{id}/redemptions`, com `rewardCode` e `requestId`.
- Histórico: `GET /students/{id}/redemptions` retorna os últimos 20 resgates.
- O débito e o registro do resgate são atômicos. Saldo insuficiente retorna 409. Repetir a mesma chave para o mesmo aluno/recompensa retorna o resgate existente, sem novo débito; reutilizar a chave para outra recompensa retorna 409.
- O catálogo é definido em código, como no ZIP. Um novo pedido com outra chave representa um novo resgate.
- `GamificationPanel.vue` substitui `RankingPanel.vue`, consumindo APIs reais de ranking, carteira, conquistas e recompensas. O painel atualiza após a conclusão e o resgate.
- O fluxo transacional de conclusão e as regras de pontos, badges e moedas estão em [parte3-conquistas-moedas.md](parte3-conquistas-moedas.md).

## Banco, Jenkins e Grafana

Os scripts `003-achievements-wallet-tables.sql` e `004-reward-tables.sql` criam as novas tabelas; `002-gamification-fks.sql` adiciona FKs também em instalações previamente criadas pelo Hibernate. Para uma instalação existente, aplicar os scripts de criação antes de iniciar a versão nova; aplicar as FKs após a existência de todas as tabelas. Não há exclusão de dados.

O Jenkins mantém os checks existentes, aplica os scripts SQL, executa Cypress em um projeto Compose descartável e verifica a presença do dashboard `b1-gamificacao`. O override E2E requer Docker Compose com suporte a `!reset`. O dashboard mostra moedas concedidas, resgates, consultas e latência do ranking. As métricas de moedas e resgates são publicadas somente após commit e representam contadores desde o início do processo, não saldos persistidos.

## Evidências desta integração

Resultados locais, usando JDK 21 e H2:

- Parte 3: 66 testes, zero falhas; PMD aprovado.
- Integração final: 70 testes, zero falhas; PMD aprovado.
- Build de produção do frontend aprovado.
- Cypress com API real e banco descartável: 5 cenários aprovados, incluindo os 2 cenários preexistentes.

Relatórios e captura mobile em [evidencias/parte4](evidencias/parte4). Os testes novos foram limitados às regras de negócio, APIs, rollback e concorrência; testes redundantes do ZIP foram removidos. As evidências antigas documentam versões anteriores e não substituem estes resultados.

Docker/PostgreSQL, execução do job Jenkins e visualização do Grafana não foram executados neste computador, que não dispõe do comando Docker. Os ajustes desses componentes estão versionados para validação no ambiente da equipe. O benchmark salvo usa MockMvc/H2, não representa capacidade de produção.
