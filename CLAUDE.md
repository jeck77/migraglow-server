# CLAUDE.md

이 문서는 이 저장소에서 작업할 때 Claude Code(claude.ai/code)가 참고할 가이드다.

## 명령어

- 빌드: `./gradlew build`
- 앱 실행: `./gradlew bootRun` (`application.properties`의 `spring.profiles.active`로 설정된 `local` 프로필 사용)
- 전체 테스트 실행: `./gradlew test`
- 단일 테스트 클래스 실행: `./gradlew test --tests "io.migraflow.MigraflowServerApplicationTests"`
- 로컬 MySQL 실행(`local` 프로필에 필요): `docker-compose up -d`

## 아키텍처

Spring Boot 4.1 / Java 21 프로젝트로, `io.migraflow.<feature>` 하위에 **기능별 패키지(package-by-feature)** 구조로 구성한다. 예: `io.migraflow.project`

- `controller` — `@RestController`. 요청/응답만 처리하고 `service`에 위임한다
- `service` — `@Service`, 클래스 레벨에 `@Transactional`을 붙이고, 조회 전용 메서드는 개별적으로 `@Transactional(readOnly = true)`를 붙인다
- `domain` — JPA 엔티티. 생성자는 `protected`(Lombok `@NoArgsConstructor(access = PROTECTED)`)이고, 상태 변경은 setter 대신 명시적 메서드(예: `Project.changeStatus(...)`)를 통해 이뤄진다
- `dto` — 요청/응답 DTO. 도메인 객체와 상호 변환한다(예: `ProjectResponse(Project project)`)
- `repository` — Spring Data JPA 인터페이스

enum 기반 상태 필드는 엔티티에 `Integer` 컬럼으로 저장되고, getter에서 enum으로 변환한다(`Project.status` / `ProjectStatus.fromCode` 참고) — 이렇게 하면 원본 DB 값과 타입이 있는 enum 모두에 접근할 수 있다.

### 메서드 문서화

private 헬퍼, 엔티티 상태 변경 메서드, JPA 리포지토리 쿼리 메서드를 포함한 모든 메서드에는 무엇을 하는지 설명하는 `/** ... */` Javadoc 주석을 바로 위에 한글로 작성한다. 도메인 의미를 지니는 생성자(엔티티의 public 생성자, DTO의 `ResponseDto(Entity entity)` 변환 생성자 등)도 동일하게 문서화한다. 필요한 경우 `@param`, `@return`, `@throws` 태그를 사용한다 — 기대되는 스타일은 `MigrationJobService`, `Project`, `MigrationJobStatus`를 참고. DTO `record` 선언도 `record` 키워드 바로 위에 컴포넌트마다 `@param` 하나씩 붙인 Javadoc 블록을 작성한다(`MigrationJobResponse`, `ProjectCreateRequest` 참고).

**이미 문서화된 파일뿐 아니라 앞으로 추가되는 모든 코드에도 적용된다**: 어느 레이어든(controller/service/domain/dto/repository) 새 메서드, 생성자, `record`를 추가할 때는 같은 커밋에서 `/** ... */` Javadoc을 함께 작성한다. 나중으로 미루지 말 것.

### 사용자에게 노출되는 메시지 텍스트는 `messages.properties`에 둔다

API 호출자에게 반환되는 한글 텍스트(예외 메시지, 검증 메시지)는 문자열 리터럴로 하드코딩하지 **않는다** — `src/main/resources/messages.properties`에 모아두고 키로 조회한다. 이는 인라인으로 유지되는 Javadoc과는 별개다.

- 도메인/서비스 코드는 예외 메시지로 **메시지 키**를 던진다. 예: `throw new IllegalStateException("migrationJob.submit.invalidState");` (`MigrationJob` 참고). 키는 `<entity>.<action>.<reason>` 형태의 lowerCamelCase를 따른다.
- 해당 텍스트는 `messages.properties`에 있다. 예: `migrationJob.submit.invalidState=등록/보정 또는 반려 상태에서만 제출할 수 있습니다.`
- 예외를 HTTP 응답으로 바꾸는 `@ExceptionHandler`는 `MessageSource` 필드를 통해 키를 해석한다(`messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale())`, 해석 실패 시 원본 키 텍스트로 폴백) — `MigrationJobController.handleIllegalState` 참고. 도메인/서비스 클래스는 `MessageSource`에 직접 의존하지 않는다(엔티티는 Spring 빈이 아니므로); 해석은 컨트롤러 경계에서만 이뤄진다.
- 앞으로 새로 추가하는 사용자 노출 메시지는 모두 이 방식을 따른다 — `throw`에 한글 텍스트를 다시 인라인으로 넣지 말 것.

### 데이터베이스 스키마는 Hibernate가 관리하지 않는다

`spring.jpa.hibernate.ddl-auto=none` — 스키마 변경은 엔티티에서 자동 생성하는 게 아니라 `sql/` 하위에 SQL 파일로 직접 작성해야 한다. `sql/`은 스크립트가 실행돼야 할 순서를 반영해 번호가 매겨진 디렉토리로 구성된다(예: `sql/01.User/`, `sql/02.Table/01.Project/`).

컬럼 명명 규칙은 `sql/01.User/01.작명규칙`에 문서화되어 있다:

| 접미사 | 의미 |
|---|---|
| `ID` | PK |
| `*_ID` | FK |
| `*_CD` | 공통코드 (common code) |
| `*_YN` | 사용 여부 (0/1 flag) |
| `*_DATE` | 타임스탬프 |
| `*_CNT` | 개수 |
| `*_SIZE` | 크기(바이트) |
| `*_PATH` | 경로 |
| `*_NAME` | 이름 |
| `*_TYPE` | 유형 |
| `*_ROLE` | 역할 |
| `*_ORDER` | 순서 |

테이블/컬럼명은 대문자를 사용하며, JPA 엔티티는 `@Table(name = "...")` / `@Column(name = "...")`으로 명시적으로 매핑한다.

## 로컬 환경

`docker-compose.yml`은 `application-local.properties`의 접속정보와 일치하는 MySQL 8.4 컨테이너(`migraflow` db/user/password: `migraflow`)를 구성한다.

## 프론트엔드

서버 렌더링 JSP + 순수 JS/CSS로, `war`로 패키징한다(React/SPA 아님):

- `build.gradle`에 `plugins { id 'war' }`, `providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'`, `implementation 'org.apache.tomcat.embed:tomcat-embed-jasper'`를 추가해 `bootRun`이 내장 Tomcat으로 JSP를 서빙하게 한다
- **알려진 Gradle 주의사항**: 이 프로젝트는 BOM 임포트에 `io.spring.dependency-management` 플러그인 대신 Gradle 네이티브 `implementation platform('org.springframework.boot:spring-boot-dependencies:4.1.0')`을 사용한다 — 해당 플러그인은 이 Spring Boot 4.1 / Spring Framework 7 조합에서 의존성을 해석할 때 `org.springframework:spring-web`을 전역적으로 제외해버려서(`--info` 로그에 `Excluding [org.springframework:spring-web]`로 나타남) `bootRun`이 `ClassNotFoundException: ...StandardServletEnvironment`로 깨진다. 이 부분을 재확인하지 않고 해당 플러그인을 다시 도입하지 말 것.
- JSP 뷰는 `src/main/webapp/WEB-INF/jsp/<feature>/*.jsp`에 있으며, `application.properties`의 `spring.mvc.view.prefix`/`suffix`로 해석된다. 각 기능별 `@Controller`(`@RestController` 아님)가 뷰 이름을 반환한다(예: `io.migraflow.project.controller.ProjectPageController`)
- 페이지는 얇은 JSP 셸 + 이미 노출된 `@RestController`들과 같은 JSON REST API를 호출하는 `fetch`로 구성된다 — JSP가 `<script>` 블록에 인라인으로 넣어 페이지 JS가 읽는 간단한 `Model` 속성(예: `projectId`) 외에는 서버 측 데이터 바인딩이 없다
- 정적 JS/CSS는 `src/main/resources/static/{js,css}`에 있다. `static/js/api.js`는 다른 페이지 스크립트(`projects.js`, `jobs.js`)가 호출하는 공용 fetch 헬퍼다 — 새 화면을 추가할 때도 페이지마다 fetch 호출을 인라인으로 넣지 말고 이 패턴을 그대로 따를 것
- 공통 헤더는 `src/main/webapp/WEB-INF/jsp/common/header.jsp`에 있고, 각 페이지 JSP는 `<%@ include file="/WEB-INF/jsp/common/header.jsp" %>`로 이 조각을 포함한다. `<header class="app-header">...</header>` 마크업을 페이지 JSP에 직접 중복 작성하지 말 것 — 새 화면을 추가할 때도 이 include를 그대로 사용한다.
- 매번 다시 입력하기 번거로운 담당자 ID는 `jobs.js`가 `localStorage`(`migraflow.actorId`)에 기억해뒀다가 폼을 열 때 자동으로 채운다. (AS-IS/TO-BE 접속정보는 더 이상 job 등록 폼에 없다 — 아래 "AS-IS 데이터 수집 방식" 참고. 예전에는 이것도 `localStorage`로 기억했지만, 접속정보 자체가 프로젝트 등록 시 한 번만 입력하는 값으로 옮겨가면서 필요 없어져 제거했다.)

## 데이터 이관 기능 (`io.migraflow.migration`)

### 도메인 개요

AS-IS(레거시) 시스템의 데이터를 TO-BE(현 시스템)로 이관하는 관리자용 웹 도구다. 배치로 한 번에 밀어넣는 게 아니라, 담당자가 웹 화면에서 이관 대상을 등록하고 값을 매핑/보정한 뒤, **검토·승인을 거쳐** 실제 반영은 별도 실행 단계에서 이뤄지는 흐름을 따른다.

### AS-IS 데이터 수집 방식

이관 대상마다 소스 방식이 다를 수 있으므로, 특정 방식에 종속되지 않게 추상화한다(`MigrationSourceType`: `DB`/`API`/`FILE`).

**현재 스코프는 `DB` 방식으로 확정**됐다. `API`/`FILE` 지원 여부는 아직 결정되지 않았다 — 결정 전까지는 DB 접속 방식을 기준으로 설계한다.

**AS-IS/TO-BE DB 접속정보는 `MigrationJob`이 아니라 `Project`에 등록한다** — 같은 프로젝트에 속한 이관 작업들은 대부분 같은 AS-IS/TO-BE 시스템을 대상으로 하므로, 매 작업마다 접속정보를 다시 입력하지 않게 하기 위해서다. `Project.sourceConfig`/`targetConfig`(JSON: `dbType`/`host`/`port`/`database`/`username`/`password`, 테이블명은 미포함)가 그 저장소다. `DbConnectionRequest`(`migration.dto`)가 이 JSON 모양과 1:1로 대응하는 DTO — jdbcUrl을 통짜로 받지 않고 필드를 나눠 받는 이유는, `dbType`/`host`/`port`/`database`/`username`은 비밀번호와 달리 화면에 다시 보여줘도 안전해서 프로젝트 수정 화면에서 그대로 다시 불러와 보여줄 수 있기 때문이다(아래 "프로젝트 수정" 참고). 실제 JDBC 접속 시에는 `MigrationSchemaService.buildJdbcUrl`이 `dbType`에 따라 벤더별 URL 형식(MySQL/PostgreSQL은 `//host:port/db`, Oracle은 `thin:@host:port:sid`, MSSQL은 `//host:port;databaseName=db`)으로 조립한다.

`DB` 방식의 흐름:

1. **프로젝트 등록 시** `projects/list.jsp`의 등록 폼에서 AS-IS/TO-BE 접속정보를 입력한다(선택 사항 — 나중에 채워도 등록 자체는 된다. 다만 등록 후 수정하는 화면은 아직 없다). "테이블 조회" 버튼(`projects.js`의 `wireConnectionTest`)으로 접속이 실제로 되는지 미리 확인할 수 있다 — 이건 `MigrationSchemaController`(`POST /api/migration-schema/tables`)를 그대로 재사용하는 stateless 검증 호출이다.
2. **프로젝트에 들어가면(= `/projects/{projectId}/jobs` 진입)** 버튼 클릭 없이 곧바로 AS-IS/TO-BE 테이블 목록이 뜬다 — `jobs.js`가 페이지 로드 시 `GET /api/projects/{projectId}/source-tables`/`target-tables`를 자동 호출한다(`ProjectSchemaService.listSourceTables`/`listTargetTables`). 이 엔드포인트는 프로젝트에 저장된 접속정보로 서버가 직접 접속하므로 브라우저에 비밀번호가 노출되지 않는다.
3. 담당자가 AS-IS/TO-BE 테이블을 각각 드롭다운에서 선택하면 `GET /api/projects/{projectId}/source-columns`/`target-columns?tableName=X`로 컬럼 목록을 보여준다 — 역시 프로젝트에 저장된 접속정보를 서버가 재사용한다.
4. **이관 작업(`MigrationJob`) 등록**은 이제 테이블 선택만 하면 된다 — `jobs.js`는 `sourceConfig`/`targetConfig`로 `{"tableName": "..."}`만 보낸다(접속정보는 이미 프로젝트에 있으므로 다시 보낼 필요가 없다). **서버가** `MigrationJobService.register`에서 프로젝트의 접속정보(`mergeProjectConnection`)와 요청의 테이블명을 합쳐, `MigrationJob.sourceConfig`/`targetConfig`에는 `dbType`/`host`/`port`/`database`/`username`/`password`/`tableName`이 모두 담긴 완전한 JSON을 저장한다(`MigrationJobService.StoredDbConfig` — `DbConnectionRequest`에 `tableName`만 더한 모양) — 그래야 컬럼 매핑 화면이 job 하나만으로 다시 접속할 수 있다(아래 "컬럼 매핑 화면" 참고). 프로젝트에 접속정보가 없는데 DB 소스로 작업을 등록하려 하면 409(`migrationJob.register.missingProjectConnection`)로 막는다.
5. 담당자가 TO-BE 테이블/컬럼도 이미 선택돼 있으므로, 컬럼 매핑 화면에서 AS-IS 컬럼과 TO-BE 컬럼을 1:1로 매핑한다 — 이름이 같아도 자동으로는 매핑되지 않고 사용자가 직접 페어링한다.
6. 매핑을 마친 뒤, 컬럼(레코드 필드)별로 보정값(수동 교정값)을 추가로 지정할 수 있다. *(미구현)*

DB 벤더는 `MYSQL`/`POSTGRESQL`/`ORACLE`/`MSSQL` 4종을 지원한다 (`build.gradle`에 `mysql-connector-j`/`postgresql`/`mssql-jdbc`/`ojdbc11` 런타임 의존성 추가, 버전은 Spring Boot BOM이 관리). 화면의 "DB 종류" 선택에 따라 입력폼이 달라진다 — 벤더별 기본 포트와 세 번째 필드 라벨(일반 DB는 "데이터베이스명", Oracle은 "SID")을 `DB_TYPE_INFO`(`projects.js`/`project-edit.js`에 있음 — 접속정보 입력 폼이 프로젝트 쪽으로 옮겨가면서 `jobs.js`에는 더 이상 없다)가 결정한다. 클라이언트는 호스트/포트/DB명/계정/비밀번호를 그대로 보내고, **jdbcUrl 조립은 서버(`MigrationSchemaService.buildJdbcUrl`)가 한다** — 예전엔 클라이언트가 jdbcUrl 문자열을 직접 만들어 보냈지만, 그러면 서버가 각 필드를 다시 꺼낼 수 없어(문자열 파싱 필요) 프로젝트 수정 화면에서 값을 다시 보여주기 까다로웠다.

`ProjectResponse`는 각 접속정보의 `dbType`/`host`/`port`/`database`/`username`은 그대로 내려주고(수정 화면에서 다시 채워 보여주기 위해), **비밀번호만** 내려주지 않는다(`sourceConfigured`/`targetConfigured` boolean만으로 등록 여부를 표시). `ProjectService.toResponse`가 저장된 JSON을 파싱해 비밀번호를 뺀 `ProjectConnectionView`로 변환한다 — 이 변환 로직은 `migration` 패키지의 `DbConnectionRequest`를 그대로 재사용하지 않고 `project.dto.ProjectConnectionView` + `ProjectService`의 private record로 따로 둔다(패키지 의존 방향을 `migration → project`로만 유지하기 위해 — `MigrationJobService`/`ProjectSchemaService`가 이미 `ProjectRepository`에 의존하므로, 반대 방향 의존까지 생기면 순환 참조가 된다).

`/projects/{projectId}/edit`(`projects/edit.jsp` + `project-edit.js`)에서 등록 후에도 이름/설명/접속정보를 수정할 수 있다 — `PUT /api/projects/{projectId}`. 진입 시 `Api.getProject()` 응답의 `source*`/`target*` 필드로 DB 종류/호스트/포트/DB명/계정을 **자동으로 다시 채워준다** — 비밀번호만 항상 빈 채로 시작한다(placeholder: "비워두면 기존 값 유지"). 저장 시 `ProjectService.update`가 **비밀번호가 비어있으면 기존 비밀번호를 그대로 이어붙이고(`mergeWithExistingPassword`), 나머지 필드는 항상 화면에 보이는 값으로 교체**한다 — 즉 호스트만 고치고 싶으면 비밀번호는 그냥 비워두면 된다. 비밀번호를 입력하면 그 값으로 완전히 교체된다. 이름/설명은 항상 요청 값으로 덮어쓴다(빈 값으로 지우는 것도 가능).

> **알려진 제약**: 접속정보(비밀번호 포함)를 평문 JSON으로 저장한다 — 이 admin 도구는 초기 단계라 암호화를 아직 적용하지 않았다.
>
> **아직 미구현**: 매핑 규칙을 실제 수집된 레코드에 자동 적용하는 것(수집 서비스 자체가 없음), 필드 수동 보정 UI, 배치 실행기.

### 컬럼 매핑 화면 (`jobs/mapping.jsp`)

작업 목록의 "컬럼 매핑" 링크(`/projects/{projectId}/jobs/{jobId}/mapping`)로 들어가는 화면. 화면 로딩 시 `MigrationJobController`의 `GET /api/migration-jobs/{jobId}/source-columns`/`target-columns`를 호출하는데, 이 두 엔드포인트는 프론트엔드가 접속정보를 다시 보내는 게 아니라 **서버가 `MigrationJob`에 저장된 sourceConfig/targetConfig JSON을 직접 읽어 JDBC로 재조회**한다 — 비밀번호가 브라우저로 다시 나가지 않는다(`MigrationJobService.listColumnsFromStoredConfig`). `ColumnInfoResponse`는 `DatabaseMetaData`로 컬럼당 `nullable`/`defaultValue`(`COLUMN_DEF`)/`primaryKey`(`getPrimaryKeys`)/`foreignKey`+`referencedTable`+`referencedColumn`(`getImportedKeys`)까지 함께 내려준다 — `jobs.js`/`mapping.js`의 `formatColumnMeta`가 이를 "VARCHAR · NOT NULL · FK → PROJECT.ID" 같은 한 줄 요약으로 보여준다. TO-BE 컬럼 중 기본키(PK)는 화면에서 매핑 대상에서 제외된다(시퀀스/auto_increment로 채번되므로) — 다만 이 차단은 화면 단이며, `MigrationMappingRule`은 job과 무관하게 `targetEntityName` 기준으로 재사용되는 규칙이라 API 레벨에서는 PK 여부를 알 방법이 없다(강제하려면 나중에 별도 설계 필요).

TO-BE 컬럼별로 매핑 유형을 고를 수 있다: `DIRECT`(AS-IS 컬럼 그대로, 보정 없음), `VALUE_MAP`(AS-IS 값 → TO-BE 값 치환표 — **지정한 값 쌍에 없는 AS-IS 값은 변환하지 않고 그대로 통과**시킨다), `FIXED_VALUE`(AS-IS 소스 없이 고정 보정값), 아무것도 선택하지 않으면("매핑 안함") 해당 TO-BE 컬럼은 반영되지 않는다 — 화면의 `RULE_TYPE_HINTS`(`mapping.js`)가 유형별로 이 동작을 문구로 보여준다. (아직 매핑 규칙을 실제 레코드에 적용하는 서비스가 없어 이 fallback 동작은 현재는 화면 문구로만 명시된 설계 의도이고, 나중에 적용 로직을 구현할 때 그대로 따라야 한다.) 같은 (targetEntityName, targetFieldName) 조합으로 다시 저장하면 기존 규칙과 그 값 매핑은 지우고 새로 만든다(`MigrationMappingRuleService.create`) — Hibernate flush 순서상 DELETE가 INSERT보다 늦게 나가면 유니크 제약(`UK_MIGRATION_MAPPING_RULE_TARGET`) 위반이 나므로, 삭제 직후 `migrationMappingRuleRepository.flush()`로 즉시 반영한다.

`MigrationJobResponse`는 sourceConfig/targetConfig JSON에서 비밀번호는 빼고 `sourceTableName`/`targetTableName`만 추출해 함께 내려준다(`MigrationJobService.toResponse`/`extractTableName` — 파싱 실패·미선택 시 null, 예외를 던지지 않고 화면 표시만 비워둔다). 컬럼 매핑 화면 상단에 AS-IS/TO-BE 테이블명을 명확히 보여주고, 작업 목록에도 함께 표시한다. 이 테이블명은 `MigrationJob.targetEntityName`(매핑 규칙을 묶는 키, 등록 시 자유 텍스트로 입력)과는 별개의 값이라 서로 다를 수 있다는 점에 유의 — 둘 다 화면에 같이 보여준다.

### 보정(보정값)

두 레이어로 구분해서 저장/추적한다 — 어떤 값이 자동 매핑된 값이고 어떤 값이 사람이 손댄 값인지 나중에 구분할 수 있어야 한다.

1. **매핑/변환 규칙** (`MigrationMappingRule`, CRUD 구현됨) — AS-IS 컬럼(`sourceFieldName`) → TO-BE 컬럼(`targetFieldName`) 매핑과 값 변환 규칙(`ruleType`: `DIRECT`/`VALUE_MAP`/`EXPRESSION`/`FIXED_VALUE`). `FIXED_VALUE`는 AS-IS 소스 없이 `expression`에 고정값 자체를 담으므로 `sourceFieldName`이 null이다. 이관 대상 간 재사용 가능하다.
2. **필드 단위 수동 교정** (`MigrationRecordField`, 엔티티만 존재) — 매핑 적용 후에도 담당자가 개별 레코드의 특정 필드 값을 화면에서 직접 수정할 수 있다.

레코드 필드에는 원본값(`rawValue`) / 매핑 후 값(`mappedValue`) / 수동 보정값(`manualValue`) / 최종값(`finalValue`)을 각각 구분해서 남긴다.

### 증분 이관 & 키 매핑 추적

같은 `MigrationJob`을 다시 실행했을 때 AS-IS에 새로 생긴 행만 추가로 수집할 수 있어야 한다. `MigrationRecord`에 `(JOB_ID, SOURCE_KEY)` 유니크 제약(`UK_MIGRATION_RECORD_JOB_SOURCE_KEY`)이 걸려 있어, 향후 수집 서비스는 새 행을 넣기 전에 `MigrationRecordRepository.existsByJobIdAndSourceKey`로 이미 수집된 키인지 확인하고 건너뛰면 된다.

- **지원 범위**: "AS-IS에 새로 생긴 행만 추가 수집"까지만 지원한다.
- **추후 과제**: "이미 수집한 행인데 AS-IS 쪽 값이 그 사이 바뀐 경우"를 감지해서 재수집하는 변경 감지는 이번 스코프가 아니다 — `updated_at`/체크섬 비교 같은 별도 설계가 필요하며 아직 미구현이다.

`MigrationRecord`는 TO-BE 반영 후의 키 값도 추적한다: `targetKey`(TO-BE 반영 후 생성/확인된 키, 미실행 시 null)와 `executionId`(어느 `MigrationExecutionHistory` 회차에서 처리됐는지, 성공/실패 모두 기록)를 `markExecuted(targetKey, executionId)` / `markFailed(errorMessage, executionId)`로 채운다. **조회 범위는 해당 `MigrationJob` 안으로 한정**된다 — 다른 job이나 다른 실행을 넘나드는 전역 키 매핑 조회는 지원하지 않으며, 필요해지면 별도 재사용 가능한 매핑 테이블을 다시 설계해야 한다.

### 실행 흐름: 등록 → 검토 → 승인 → 배치 실행

즉시 반영하지 않는다. 아래 단계로 분리한다.

1. **등록** — 담당자가 이관 대상을 등록하고 AS-IS 데이터를 수집한다.
2. **보정** — 매핑 규칙이 자동 적용되고, 필요 시 담당자가 필드를 수동 교정한다.
3. **제출 / 승인 대기** — 담당자가 제출하면 승인 대기 상태로 전환된다.
4. **승인** — 승인자가 검토 후 승인(또는 반려)한다. 승인자와 승인 시각을 기록해 감사 추적이 가능해야 한다.
5. **배치 실행** — 승인된 건만 별도 배치/스케줄러가 실제 TO-BE 반영을 수행한다. 웹 등록 트랜잭션과 실제 반영 트랜잭션은 분리되어 있다.

반려된 건은 다시 보정 단계로 되돌아갈 수 있어야 한다.

### 도메인 모델 (`io.migraflow.migration.domain`, 구현됨)

- `MigrationJob` — 이관 작업 단위 (대상 시스템, 소스 타입, 상태). 등록 → 제출 → 승인/반려 흐름과 `MigrationJobApprovalHistory` 이력 기록까지 `MigrationJobService`에 구현돼 있다.
- `MigrationRecord` / `MigrationRecordField` — 개별 이관 대상 레코드와 필드 단위 값(원본값 / 매핑 후 값 / 수동 보정값 / 최종값, 상태). 엔티티만 존재하고, 실제로 AS-IS를 수집해 레코드를 채우는 서비스 로직은 아직 없다.
- `MigrationMappingRule` — 필드별 AS-IS → TO-BE 매핑/변환 규칙. 등록/조회/비활성화는 `MigrationMappingRuleService`에 구현돼 있다(컬럼 매핑 화면에서 사용). 다만 이 규칙을 실제 수집된 `MigrationRecord`에 적용해 `MigrationRecordField`를 채우는 로직은 아직 없다(수집 서비스 자체가 없으므로).
- `MigrationExecutionHistory` — 배치 실행 이력. 엔티티만 존재하고, 배치 실행 자체(`MigrationJob.startExecution/completeExecution/failExecution` 호출)는 아직 서비스에 연결돼 있지 않다.

상태는 위 컨벤션대로 `Integer` 컬럼 + enum 패턴을 따른다 (예: `MigrationJob.status` / `MigrationJobStatus.fromCode`).

`MigrationJob` 상태값: `DRAFT`(등록/보정 중) → `SUBMITTED`(승인 대기) → `APPROVED` / `REJECTED` → `EXECUTING` → `COMPLETED` / `FAILED`.

새 테이블은 `sql/` 하위에 번호를 이어 디렉토리를 추가한다 (예: `sql/02.Table/0X.Migration/`).
