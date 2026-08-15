package io.migraflow.migration.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * AS-IS/TO-BE DB 접속 정보. 테이블·컬럼 조회 및 {@code MigrationJob}/{@code Project} 등록 시 소스/타겟 설정으로 함께 쓰인다.
 * 필드를 개별로 나눠 저장해두면(호스트/포트/DB명/계정은 비밀번호와 달리 화면에 다시 보여줘도 안전하므로) 프로젝트 수정
 * 화면에서 비밀번호만 빼고 나머지는 그대로 다시 불러올 수 있다.
 *
 * @param dbType   DB 벤더 구분 (MYSQL/POSTGRESQL/ORACLE/MSSQL). 표시용이면서, 실제 JDBC URL을 조립하는 데도 쓰인다
 * @param host     호스트
 * @param port     포트 (비어있으면 벤더 기본 포트를 사용한다)
 * @param database 데이터베이스명(Oracle은 SID)
 * @param username 접속 계정
 * @param password 접속 비밀번호. 빈 값이면 "기존 비밀번호 유지"를 의미하는 문맥(프로젝트 수정)이 있으므로 필수로 두지 않는다
 */
public record DbConnectionRequest(
        @NotBlank String dbType,
        @NotBlank String host,
        String port,
        @NotBlank String database,
        @NotBlank String username,
        String password
) {
}
