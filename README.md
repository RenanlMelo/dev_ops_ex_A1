# dev_ops_ac1

[![CI](https://github.com/RenanlMelo/dev_ops_ex_A1/actions/workflows/ci.yml/badge.svg)](https://github.com/RenanlMelo/dev_ops_ex_A1/actions/workflows/ci.yml)

Atividade de DevOps (BDD + TDD, ou seja, ATDD) - API de matricula em cursos em Spring Boot, com front-end em
Vue, implementando o case "Educacao Continuada Gamificada" apresentado em aula.

## Estudo de caso

Plataforma de EAD (Educacao a Distancia) gamificada: o aluno se matricula em um curso, o conclui com uma nota, e
- se a media final for maior que 7,0 - passa a ter direito a realizar mais 3 cursos extras. Antes do curso ser
concluido, o aluno nao tem acesso a sua propria media.

### User Story escolhida pelo grupo

US4 - Como aluno, quero que minha media seja calculada ao concluir um curso, para saber se tenho direito a
cursos extras (o acesso a media so e liberado apos a conclusao do curso).

### BDDs por integrante

Cada integrante do grupo escreveu um cenario BDD (Given/When/Then) para a US4, usando a si mesmo como
participante de exemplo. Os tres cenarios estao implementados como testes em
[CourseServiceTest.java](backend/src/test/java/com/example/devopsac1/Test/CourseServiceTest.java):

| Integrante | BDD |
|---|---|
| Kevin | Dado um curso e um Participante valido, quando o curso for finalizado e a nota for acima de 7,0, entao o usuario tem direito a realizacao de mais 3 cursos. |
| Renan | Dado um curso e um Participante valido, quando o curso for finalizado e a nota for abaixo de 7,0, entao o usuario nao tera direito a realizacao de mais 3 cursos. |
| Roberto | Dado um curso e um Participante valido, enquanto o curso nao for concluido, entao o usuario nao tera acesso a sua media do curso. |

## Estrutura do repositorio

```
backend/    API Spring Boot (Java 24, JPA, H2, Postgres)
frontend/   SPA Vue 3 + Vite que consome a API
docs/tdd-evidence/   Evidencias do ciclo RED-GREEN-BLUE (prints + relatorios JaCoCo)
```

Dentro do backend, o codigo segue por camadas:

```
Domain/       Entidades de dominio (Course, Student, Enrollment) - @Entity JPA
Repository/   Spring Data JPA (CourseRepository, StudentRepository, EnrollmentRepository)
Service/      Regra de negocio (CourseService)
Dto/          Records de request/response da API
Controller/   Endpoints REST (CourseController, StudentController, EnrollmentController)
Exception/    Tratamento de erro centralizado (404/409/400)
Config/       Configuracao do Swagger/OpenAPI
```

## TDD: ciclo RED -> GREEN -> BLUE

Aplicado em [Enrollment.hasGradeAboveMinimum()](backend/src/main/java/com/example/devopsac1/Domain/Enrollment.java),
testado diretamente por
[EnrollmentTest](backend/src/test/java/com/example/devopsac1/DomainTest/EnrollmentTest.java) (pacote DomainTest, teste de dominio, no
mesmo estilo do exercicio da calculadora - sem passar por Spring, so instancia a classe e chama o metodo).
Evidencias completas (saida de teste + relatorio HTML do JaCoCo) em [docs/tdd-evidence/](docs/tdd-evidence/):

| Etapa | O que foi feito | Resultado | Evidencia |
|---|---|---|---|
| RED | Implementacao com bug proposital (grade >= MINIMUM em vez de >, sem tratar completed/grade nulos) | 2 de 7 testes falham (1 failure + 1 error) | [1-red-test-output.png](docs/tdd-evidence/1-red-test-output.png) |
| GREEN | Implementacao minima correta (cadeia de ifs) | 7/7 testes passam, mas 1 branch fica amarelo (checagem de grade == null nunca e exercitada, pois e um estado impossivel do dominio) | [2-green-coverage-enrollment.png](docs/tdd-evidence/2-green-coverage-enrollment.png), [relatorio completo](docs/tdd-evidence/2-green-jacoco/com.example.devopsac1.Domain/Enrollment.java.html) |
| BLUE | Refactor: remove a checagem de nulo redundante (o dominio garante grade != null sempre que completed == true, pois complete() define os dois juntos) | 7/7 testes passam, 100% de instrucoes/linhas/branches cobertos, sem amarelo ou vermelho | [3-blue-coverage-enrollment.png](docs/tdd-evidence/3-blue-coverage-enrollment.png), [relatorio completo](docs/tdd-evidence/3-blue-jacoco/com.example.devopsac1.Domain/Enrollment.java.html) |

Pra reproduzir localmente:

```bash
cd backend
./mvnw clean verify
# relatorio gerado em target/site/jacoco/index.html
```

## Rodando localmente

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

H2 console em `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/demodb`, user `sa`, senha em
branco).

Frontend (em outro terminal, com o backend rodando):

```bash
cd frontend
npm install
npm run dev
```

Abre em `http://localhost:5173`. O Vite faz proxy de `/api/*` para `http://localhost:8080/*` (veja
`frontend/vite.config.js`), entao o front chama a API real sem precisar de CORS no backend.

## Rodando com Docker

```bash
docker compose up --build
```

Sobe 3 containers:

| Servico | Porta | Descricao |
|---|---|---|
| app | 8080 | API Spring Boot (H2 por padrao) |
| postgres | 5432 | Postgres 16, banco devopsac1 |
| pgadmin | 5050 | pgAdmin4 (admin@admin.com / admin) para administrar o Postgres |

Por padrao a aplicacao usa H2 (arquivo em ./data). Pra rodar contra o Postgres do compose em vez do H2,
descomente as 4 variaveis SPRING_DATASOURCE_* de Postgres no [.env.example](.env.example) (copie pra .env) e
suba de novo com `docker compose up --build` - o Hibernate cria o schema automaticamente no Postgres tambem
(ddl-auto=update), sem precisar de migracao manual.

Evidencia real: subimos uma instancia apontada para o Postgres do compose, criamos um curso via
`POST /courses`, e confirmamos tanto via `psql` quanto via pgAdmin (Databases > devopsac1 > Schemas > public >
Tables) que as 3 tabelas (course, enrollment, student) foram criadas pelo Hibernate e o registro aparece la -
ver [docs/postgres-evidence/tables.txt](docs/postgres-evidence/tables.txt).

Evidencias de H2 e Postgres rodando: `docs/tdd-evidence/` (ver secao acima) mostra a suite completa passando
contra o H2 em memoria; o proprio `docker compose up` sobe o Postgres + pgAdmin lado a lado com o H2 do app.

## API

Documentacao interativa (Swagger UI) em `http://localhost:8080/swagger-ui/index.html`
(OpenAPI JSON cru em `/v3/api-docs`). Health check do Actuator em `/actuator/health`.

| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET    | `/courses?page=&size=` | Lista cursos (paginado) |
| GET    | `/courses/{id}` | Detalhe de um curso |
| POST   | `/courses` | Cria um curso |
| PUT    | `/courses/{id}` | Renomeia um curso |
| DELETE | `/courses/{id}` | Remove um curso |
| GET    | `/courses/{id}/enrollments` | Turma (matriculas) do curso |
| GET    | `/courses/{id}/average` | Media da turma (so matriculas concluidas) |
| GET    | `/students?page=&size=` | Lista alunos (paginado) |
| GET    | `/students/{id}` | Detalhe de um aluno |
| POST   | `/students` | Cria um aluno |
| PUT    | `/students/{id}` | Renomeia um aluno |
| DELETE | `/students/{id}` | Remove um aluno |
| GET    | `/students/{id}/enrollments` | Historico de matriculas do aluno |
| GET    | `/students/{id}/eligible-for-extra-courses` | Se o aluno tem direito a cursos extra |
| POST   | `/enrollments` | Matricula um aluno em um curso |
| GET    | `/enrollments/{id}` | Detalhe de uma matricula |
| POST   | `/enrollments/{id}/complete` | Conclui a matricula com uma nota (0-10) |
| GET    | `/enrollments/{id}/average` | Nota final (409 se ainda nao concluida) |

Erros seguem um formato unico (status, error, message, timestamp): 404 pra recurso inexistente, 409 pra
regra de negocio violada (ex: pedir media de matricula nao concluida), 400 pra validacao de request (ex: nota fora
de 0-10).

## Configuracao

Todas as configs relevantes do backend podem ser sobrescritas por variavel de ambiente - veja
[.env.example](.env.example). Por padrao (perfil dev, sem SPRING_PROFILES_ACTIVE definido), a aplicacao sobe
com:

- console do H2 acessivel em /h2-console (login sa, sem senha)
- log de SQL habilitado

Isso e intencional para desenvolvimento/teste, mas nao e o comportamento desejavel em um deploy real. Para
desligar o console do H2 e o log de SQL, defina explicitamente:

```bash
SPRING_PROFILES_ACTIVE=prod
```

(veja backend/src/main/resources/application-prod.properties). O docker-compose.yml nao ativa esse perfil por
padrao - e preciso setar a variavel de ambiente manualmente (via .env ou -e no docker run).

## Testes

```bash
cd backend
./mvnw test
```

54 testes no total: testes de dominio/servico (JUnit puro, sem Spring) e testes de integracao dos controllers
(JUnit + MockMvc + banco H2 em memoria, sem mocks de repositorio).
