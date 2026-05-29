# label-cat — AGENTS.md

## Stack

- **Spring Boot 4.0.6** — Java 25, Maven 3.9.16 (wrapper: `./mvnw`)
- Package base: `com.br.lvs_group.label_cat`
- Grupo Maven: `com.br.lvs-group` (hífen) → pacote: `lvs_group` (underline) — restrição do Java
- Dependências: Data JPA, Security, Validation, Lombok 1.18.46, Web, PostgreSQL, Docker Compose
- **Banco dev:** PostgreSQL 16 via Docker Compose (`docker-compose.yml`)
- **Banco test:** H2 em memória (`src/test/resources/application.properties`)

## Entidades

- `User` → tabela `users` — id, name, email (unique), password, function, created_at, updated_at
- `TypeOfLabel` → tabela `types_of_labels` — id, name (unique), description, created_at, updated_at
- `Label` → tabela `labels` — id, name, code_ref (unique), type (FK → types_of_labels), createdBy (FK → users), qtd_by_batch, image_url, localization, fields (JSONB), sector, created_at, updated_at

## Repositórios

Todos estendem `JpaRepository`. `UserRepository` também estende `JpaSpecificationExecutor` para filtros dinâmicos.
`LabelRepository` possui `findByCodeRef(String codeRef)`.

## Comandos

| Ação | Comando |
|---|---|
| Rodar (dev) | `docker compose up -d` + `./mvnw spring-boot:run` |
| Rodar (tudo) | `./mvnw spring-boot:run` (Docker Compose automático) |
| Testes | `./mvnw test` (usa H2, sem Docker) |
| Compilar | `./mvnw clean compile` |
| Build | `./mvnw clean install` |

## Estado do projeto

- Camada de dados implementada: entidades + repositórios + Docker Compose + PostgreSQL
- `src/test/resources/application.properties` configura H2 para testes
- `spring.jpa.hibernate.ddl-auto=update` — Hibernate gerencia o schema
- `spring.jpa.show-sql=true` — exibe SQL no console
- Sem CI/CD, linter, formatter, `.editorconfig`
- `application.properties` com secrets (jwt.secret, DB password) está no `.gitignore`
- Use `application.properties.example` como template para configurar localmente

## Git

- Repositório: `https://github.com/LeonardoSodre20/label-catalog.git`
- Commits semânticos seguindo conventional commits
- **Nunca executar `git push`** — somente o usuário faz push manualmente

## Endpoints com paginação

- `GET /api/users` — suporta `?name=`, `?email=`, `?function=`, `?page=`, `?size=`, `?sort=`
