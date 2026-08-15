package io.migraflow.migration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 특정 테이블의 컬럼 목록 조회 요청.
 *
 * @param connection 조회 대상 DB 접속 정보
 * @param tableName  컬럼 목록을 조회할 테이블명
 */
public record MigrationSchemaColumnsRequest(
        @NotNull @Valid DbConnectionRequest connection,
        @NotBlank String tableName
) {
}
