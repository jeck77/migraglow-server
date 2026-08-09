# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

- Build: `./gradlew build`
- Run the app: `./gradlew bootRun` (uses `local` profile, set via `spring.profiles.active` in `application.properties`)
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "io.migraflow.MigraflowServerApplicationTests"`
- Start local MySQL (required for the `local` profile): `docker-compose up -d`

## Architecture

Spring Boot 4.1 / Java 21 project, organized **package-by-feature** under `io.migraflow.<feature>`, e.g. `io.migraflow.project`:

- `controller` — `@RestController`, request/response only, delegates to `service`
- `service` — `@Service`, `@Transactional` at the class level, read-only queries annotated per-method with `@Transactional(readOnly = true)`
- `domain` — JPA entities. Constructors are `protected` (via Lombok `@NoArgsConstructor(access = PROTECTED)`), state changes go through explicit methods (e.g. `Project.changeStatus(...)`) rather than setters
- `dto` — request/response DTOs, converted to/from domain objects (e.g. `ProjectResponse(Project project)`)
- `repository` — Spring Data JPA interfaces

Enum-backed status fields are stored as `Integer` columns on the entity, with a getter that converts via the enum (see `Project.status` / `ProjectStatus.fromCode`), so the raw DB value and the typed enum both stay accessible.

### Database schema is NOT managed by Hibernate

`spring.jpa.hibernate.ddl-auto=none` — schema changes must be written by hand as SQL files under `sql/`, not generated from entities. `sql/` is organized in numbered directories mirroring the order scripts should run in (e.g. `sql/01.User/`, `sql/02.Table/01.Project/`).

Column naming follows the convention documented in `sql/01.User/01.작명규칙`:

| Suffix | Meaning |
|---|---|
| `ID` | PK |
| `*_ID` | FK |
| `*_CD` | 공통코드 (common code) |
| `*_YN` | 사용 여부 (0/1 flag) |
| `*_DATE` | timestamp |
| `*_CNT` | count |
| `*_SIZE` | size in bytes |
| `*_PATH` | path |
| `*_NAME` | name |
| `*_TYPE` | type |
| `*_ROLE` | role |
| `*_ORDER` | order |

Table and column names are uppercase; JPA entities map to them explicitly via `@Table(name = "...")` / `@Column(name = "...")`.

## Local environment

`docker-compose.yml` provisions a MySQL 8.4 container (`migraflow` db/user/password: `migraflow`) matching the credentials in `application-local.properties`.
