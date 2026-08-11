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

## Frontend

Server-rendered JSP + vanilla JS/CSS, packaged as a `war` (not React/SPA):

- `plugins { id 'war' }` in `build.gradle`, with `providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'` and `implementation 'org.apache.tomcat.embed:tomcat-embed-jasper'` so `bootRun` serves JSPs via embedded Tomcat
- **Known Gradle gotcha**: this project uses Gradle's native `implementation platform('org.springframework.boot:spring-boot-dependencies:4.1.0')` for BOM import instead of the `io.spring.dependency-management` plugin — that plugin globally excludes `org.springframework:spring-web` when resolving against this Spring Boot 4.1 / Spring Framework 7 combination (visible as `Excluding [org.springframework:spring-web]` in `--info` logs), which breaks `bootRun` with `ClassNotFoundException: ...StandardServletEnvironment`. Don't reintroduce that plugin without re-checking this.
- JSP views live in `src/main/webapp/WEB-INF/jsp/<feature>/*.jsp`, resolved via `spring.mvc.view.prefix`/`suffix` in `application.properties`; a `@Controller` (not `@RestController`) per feature returns the view name, e.g. `io.migraflow.project.controller.ProjectPageController`
- Pages are a thin JSP shell + `fetch` calls to the same JSON REST API the `@RestController`s already expose — no server-side data binding beyond simple `Model` attributes (e.g. `projectId`) the JSP inlines into a `<script>` block for the page's JS to read
- Static JS/CSS live in `src/main/resources/static/{js,css}`; `static/js/api.js` is the shared fetch helper other page scripts (`projects.js`, `jobs.js`) call into — mirror that pattern for new screens rather than inlining fetch calls per page

## Data Migration Feature (`io.migraflow.migration`)

### Domain overview

AS-IS(레거시) 시스템의 데이터를 TO-BE(현 시스템)로 이관하는 관리자용 웹 도구다. 배치로 한 번에 밀어넣는 게 아니라, 담당자가 웹 화면에서 이관 대상을 등록하고 값을 매핑/보정한 뒤, **검토·승인을 거쳐** 실제 반영은 별도 실행 단계에서 이뤄지는 흐름을 따른다.

### AS-IS 데이터 수집 방식

이관 대상마다 소스 방식이 다를 수 있으므로, 특정 방식에 종속되지 않게 추상화한다.

- `DB`: AS-IS DB에 read-only로 직접 접속해 JDBC로 조회
- `API`: AS-IS가 제공하는 API를 호출해 수집
- `FILE`: 업로드된 CSV/Excel 등의 파일을 파싱

소스 타입은 `MigrationSourceType` 같은 enum으로 구분하고, 수집 로직은 소스 타입별 전략(strategy)으로 분리한다.

### 보정(보정값)

두 레이어로 구분해서 저장/추적한다 — 어떤 값이 자동 매핑된 값이고 어떤 값이 사람이 손댄 값인지 나중에 구분할 수 있어야 한다.

1. **매핑/변환 규칙** — AS-IS 값 → TO-BE 값 자동 변환 (예: 상태 코드 매핑 테이블). 이관 대상 간 재사용 가능해야 한다.
2. **필드 단위 수동 교정** — 매핑 적용 후에도 담당자가 개별 레코드의 특정 필드 값을 화면에서 직접 수정할 수 있다.

레코드에는 원본값 / 매핑 후 값 / 수동 보정값 / 최종값을 각각 구분해서 남긴다.

### 실행 흐름: 등록 → 검토 → 승인 → 배치 실행

즉시 반영하지 않는다. 아래 단계로 분리한다.

1. **등록** — 담당자가 이관 대상을 등록하고 AS-IS 데이터를 수집한다.
2. **보정** — 매핑 규칙이 자동 적용되고, 필요 시 담당자가 필드를 수동 교정한다.
3. **제출 / 승인 대기** — 담당자가 제출하면 승인 대기 상태로 전환된다.
4. **승인** — 승인자가 검토 후 승인(또는 반려)한다. 승인자와 승인 시각을 기록해 감사 추적이 가능해야 한다.
5. **배치 실행** — 승인된 건만 별도 배치/스케줄러가 실제 TO-BE 반영을 수행한다. 웹 등록 트랜잭션과 실제 반영 트랜잭션은 분리되어 있다.

반려된 건은 다시 보정 단계로 되돌아갈 수 있어야 한다.

### Suggested domain model (draft — 구현 전 논의 필요)

- `MigrationJob` — 이관 작업 단위 (대상 시스템, 소스 타입, 상태)
- `MigrationRecord` — 개별 이관 대상 레코드 (원본값 / 매핑 후 값 / 수동 보정값 / 최종값, 상태)
- `MigrationMappingRule` — 필드별 AS-IS → TO-BE 매핑/변환 규칙

상태는 위 컨벤션대로 `Integer` 컬럼 + enum 패턴을 따른다 (예: `MigrationJob.status` / `MigrationJobStatus.fromCode`).

`MigrationJob` 상태값 후보: `DRAFT`(등록/보정 중) → `SUBMITTED`(승인 대기) → `APPROVED` / `REJECTED` → `EXECUTING` → `COMPLETED` / `FAILED`.

새 테이블은 `sql/` 하위에 번호를 이어 디렉토리를 추가한다 (예: `sql/02.Table/0X.Migration/`).
