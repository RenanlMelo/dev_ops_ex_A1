# Educação Continuada Gamificada

## Parte 2 Ranking

Branch `codex/b1-parte2-ranking`, baseada na parte 1. Acrescenta ranking persistido e indexado, paginação, posição individual, desempate por ID e painel lateral. Pontos = nota × 10; atualizar a nota substitui os pontos. Matrícula atualiza Scoreboard na mesma transação por EnrollmentRankingUpdater; RankingBootstrap recompõe conclusões antigas. Os adaptadores serão substituídos ao integrar conquistas, sem duplicar pontuações.

Validação local: 48 testes Java aprovados, PMD sem violações e build do frontend Docker. Esta entrega não contém moedas, badges ou recompensas. A documentação histórica abaixo permanece como registro das atividades anteriores.
## Parte 1 Área do aluno e cursos

Branch `codex/b1-parte1-aluno-cursos`: perfil local persistente, seis cursos (cinco concluídos e um em andamento inicialmente), busca, filtros, avaliação final e resultados. As notas só aparecem após a conclusão. O perfil demonstrativo não implementa autenticação. `app.demo.enabled=false` desativa sua inicialização.

Execute `docker compose -f docker-compose.yml -f docker-compose.homol.yml up -d --build` com as senhas configuradas em `.env`. Frontend em http://localhost:8082. O progresso não é redefinido no reload.

Validação local: 44 testes Java aprovados, PMD sem violações e build do frontend Docker. A parte 2 adicionará o ranking; moedas, conquistas e recompensas ficam para as partes 3 e 4.

Documentação histórica: [AC1](<AC1 README.md>), [AC2](<AC2 README.md>) e [arquitetura B1](docs/arquitetura/README.md).
