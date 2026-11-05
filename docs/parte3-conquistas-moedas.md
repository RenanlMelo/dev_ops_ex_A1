# Parte 3 — Conquistas e moedas

Base: `origin/codex/b1-parte2-ranking` (`76a9bbb`). Branch: `codex/b1-parte3-conquistas-moedas`.

A conclusão da matrícula chama `CompletionAwardCommand` na mesma transação de pontos, badges, carteira e ranking. Qualquer falha reverte o conjunto. `EnrollmentRankingUpdater` e `RankingBootstrap` foram substituídos pelo serviço de conquistas e pelo replay idempotente `HistoricalGamificationBootstrap`.

## Regras

- Pontos: arredondamento de nota × 10; notas entre 0 e 10. A revisão substitui pontos, preservando o contrato do ranking.
- Aprovação: nota acima de 7, mantendo a regra preexistente.
- Badges permanentes: primeira aprovação, três aprovações e nota 10.
- Crédito de 50 moedas por aluno/curso, com referência única. Revisões e repetição da requisição não concedem moedas adicionais; rebaixar uma nota não revoga moedas ou badges já conquistados.
- Matrículas repetidas para o mesmo aluno/curso retornam a matrícula existente. Locks serializam conclusões e movimentações por aluno.

## APIs

`GET /students/{id}/achievements` retorna pontos, contagens e catálogo de badges com data de obtenção. `GET /students/{id}/wallet` retorna saldo e as 20 últimas movimentações. O saldo considera todo o extrato.

## Banco e validação

Aplicar `docs/arquitetura/sql/003-achievements-wallet-tables.sql` no PostgreSQL antes do primeiro início. Não remove dados. Para bancos já criados pelo Hibernate, aplicar também as FKs da entrega integrada. O replay de matrículas concluídas pode ser desativado com `gamification.backfill.enabled=false`.

Testes: `AchievementApiTest`, `AchievementTransactionTest`, `GamificationRulesTest`, fronteiras ArchUnit e todos os testes preexistentes do ranking. Executar no JDK 21: `cd backend` e `./mvnw.cmd -B clean verify pmd:check`.
