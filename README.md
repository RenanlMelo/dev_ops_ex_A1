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
