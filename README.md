# dev_ops_ac1

[![CI](https://github.com/RenanlMelo/dev_ops_ex_A1/actions/workflows/ci.yml/badge.svg)](https://github.com/RenanlMelo/dev_ops_ex_A1/actions/workflows/ci.yml)

API de matricula em cursos (Spring Boot + JPA + H2), usada como exercicio de DevOps.

## Rodando localmente

```bash
./mvnw spring-boot:run
```

H2 console em `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/demodb`, user `sa`, senha em branco).

## Rodando com Docker

```bash
docker compose up --build
```

## API

Documentacao interativa (Swagger UI) em `http://localhost:8080/swagger-ui/index.html`
(OpenAPI JSON cru em `/v3/api-docs`). Health check do Actuator em `/actuator/health`.

| Metodo | Rota                                      | Descricao                                   |
|--------|--------------------------------------------|----------------------------------------------|
| GET    | `/courses?page=&size=`                     | Lista cursos (paginado)                      |
| GET    | `/courses/{id}`                            | Detalhe de um curso                           |
| POST   | `/courses`                                 | Cria um curso                                 |
| PUT    | `/courses/{id}`                            | Renomeia um curso                             |
| DELETE | `/courses/{id}`                            | Remove um curso                               |
| GET    | `/courses/{id}/enrollments`                | Turma (matriculas) do curso                   |
| GET    | `/courses/{id}/average`                    | Media da turma (so matriculas concluidas)     |
| GET    | `/students?page=&size=`                    | Lista alunos (paginado)                       |
| GET    | `/students/{id}`                           | Detalhe de um aluno                           |
| POST   | `/students`                                | Cria um aluno                                 |
| PUT    | `/students/{id}`                           | Renomeia um aluno                             |
| DELETE | `/students/{id}`                           | Remove um aluno                               |
| GET    | `/students/{id}/enrollments`               | Historico de matriculas do aluno              |
| GET    | `/students/{id}/eligible-for-extra-courses`| Se o aluno tem direito a cursos extra         |
| POST   | `/enrollments`                             | Matricula um aluno em um curso                |
| GET    | `/enrollments/{id}`                        | Detalhe de uma matricula                      |
| POST   | `/enrollments/{id}/complete`               | Conclui a matricula com uma nota (0-10)       |
| GET    | `/enrollments/{id}/average`                | Nota final (409 se ainda nao concluida)       |

Erros seguem um formato unico (`status`, `error`, `message`, `timestamp`): 404 pra recurso inexistente, 409 pra
regra de negocio violada (ex: pedir media de matricula nao concluida), 400 pra validacao de request (ex: nota fora
de 0-10).

## Configuracao

Todas as configs relevantes podem ser sobrescritas por variavel de ambiente - veja [.env.example](.env.example).
Por padrao (perfil `dev`, sem `SPRING_PROFILES_ACTIVE` definido), a aplicacao sobe com:

- console do H2 acessivel em `/h2-console` (login `sa`, sem senha)
- log de SQL habilitado

Isso e intencional para desenvolvimento/teste, mas **nao e o comportamento desejavel em um deploy real**. Para
desligar o console do H2 e o log de SQL, defina explicitamente:

```bash
SPRING_PROFILES_ACTIVE=prod
```

(veja `src/main/resources/application-prod.properties`). O `docker-compose.yml` nao ativa esse perfil por padrao -
e preciso setar a variavel de ambiente manualmente (via `.env` ou `-e` no `docker run`).

## Testes

```bash
./mvnw test
```
