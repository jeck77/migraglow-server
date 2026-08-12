package io.migraflow.migration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 이관 작업을 반려할 때 전달되는 요청 정보.
 *
 * @param actorId 반려자 ID
 * @param reason  반려 사유
 */
public record MigrationJobRejectRequest(
        @NotNull Long actorId,
        @NotBlank String reason
) {
}
