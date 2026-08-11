package io.migraflow.migration.dto;

import io.migraflow.migration.domain.MigrationJob;
import io.migraflow.migration.domain.MigrationJobStatus;
import io.migraflow.migration.domain.MigrationSourceType;
import java.time.LocalDateTime;

public record MigrationJobResponse(
        Long id,
        Long projectId,
        String name,
        String targetEntityName,
        MigrationSourceType sourceType,
        MigrationJobStatus status,
        Long createdById,
        Long submittedById,
        LocalDateTime submitDate,
        Long approvedById,
        LocalDateTime approveDate,
        String rejectReason,
        LocalDateTime createDate,
        LocalDateTime updatedDate
) {

    public MigrationJobResponse(MigrationJob job) {
        this(
                job.getId(),
                job.getProjectId(),
                job.getName(),
                job.getTargetEntityName(),
                job.getSourceType(),
                job.getStatus(),
                job.getCreatedById(),
                job.getSubmittedById(),
                job.getSubmitDate(),
                job.getApprovedById(),
                job.getApproveDate(),
                job.getRejectReason(),
                job.getCreateDate(),
                job.getUpdatedDate()
        );
    }
}
