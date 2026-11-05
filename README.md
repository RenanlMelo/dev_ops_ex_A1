# Educação Continuada Gamificada

## Parte 1 Área do aluno e cursos

Branch `codex/b1-parte1-aluno-cursos`: perfil local persistente, seis cursos (cinco concluídos e um em andamento inicialmente), busca, filtros, avaliação final e resultados. As notas só aparecem após a conclusão. O perfil demonstrativo não implementa autenticação. `app.demo.enabled=false` desativa sua inicialização.

Execute `docker compose -f docker-compose.yml -f docker-compose.homol.yml up -d --build` com as senhas configuradas em `.env`. Frontend em http://localhost:8082. O progresso não é redefinido no reload.

Validação local: 44 testes Java aprovados, PMD sem violações e build do frontend Docker. A parte 2 adicionará o ranking; moedas, conquistas e recompensas ficam para as partes 3 e 4.

Documentação histórica: [AC1](<AC1 README.md>), [AC2](<AC2 README.md>) e [arquitetura B1](docs/arquitetura/README.md).
