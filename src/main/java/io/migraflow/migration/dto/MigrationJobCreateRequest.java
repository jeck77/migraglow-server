package io.migraflow.migration.dto;

import io.migraflow.migration.domain.MigrationSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 신규 이관 작업 등록 요청 정보.
 *
 * @param name             작업명
 * @param targetEntityName 이관 대상 엔티티명
 * @param sourceType       AS-IS 데이터 수집 방식
 * @param sourceConfig     AS-IS 접속/조회 설정(JSON)
 * @param targetConfig     TO-BE DB 접속 설정(JSON)
 * @param createdById      등록자 ID
 */
public record MigrationJobCreateRequest(
        @NotBlank String name,
        @NotBlank String targetEntityName,
        @NotNull MigrationSourceType sourceType,
        String sourceConfig,
        String targetConfig,
        @NotNull Long createdById
) {
}
