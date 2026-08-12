package io.migraflow.migration.dto;

import io.migraflow.migration.domain.MigrationJobActionType;
import io.migraflow.migration.domain.MigrationJobApprovalHistory;
import java.time.LocalDateTime;

/**
 * 이관 작업의 제출/승인/반려 이력 조회/응답용 DTO.
 *
 * @param id         승인 이력 ID
 * @param actionType 수행된 액션 타입 (제출/승인/반려)
 * @param actorId    액션을 수행한 사용자 ID
 * @param actionDate 액션 수행 일시
 * @param reason     사유 (반려 시)
 */
public record MigrationJobApprovalHistoryResponse(
        Long id,
        MigrationJobActionType actionType,
        Long actorId,
        LocalDateTime actionDate,
        String reason
) {

    /**
     * {@link MigrationJobApprovalHistory} 엔티티를 응답 DTO로 변환한다.
     *
     * @param history 변환할 승인 이력 엔티티
     */
    public MigrationJobApprovalHistoryResponse(MigrationJobApprovalHistory history) {
        this(
                history.getId(),
                history.getActionType(),
                history.getActorId(),
                history.getActionDate(),
                history.getReason()
        );
    }
}
