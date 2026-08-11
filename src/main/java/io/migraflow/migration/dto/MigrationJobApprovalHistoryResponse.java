package io.migraflow.migration.dto;

import io.migraflow.migration.domain.MigrationJobActionType;
import io.migraflow.migration.domain.MigrationJobApprovalHistory;
import java.time.LocalDateTime;

public record MigrationJobApprovalHistoryResponse(
        Long id,
        MigrationJobActionType actionType,
        Long actorId,
        LocalDateTime actionDate,
        String reason
) {

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
