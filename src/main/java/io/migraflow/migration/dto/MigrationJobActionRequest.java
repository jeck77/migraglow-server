package io.migraflow.migration.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 이관 작업의 제출/승인 액션을 수행할 때 전달되는 요청 정보.
 *
 * @param actorId 액션을 수행하는 사용자 ID
 */
public record MigrationJobActionRequest(
        @NotNull Long actorId
) {
}
