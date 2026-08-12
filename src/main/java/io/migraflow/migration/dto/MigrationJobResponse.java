package io.migraflow.migration.dto;

import io.migraflow.migration.domain.MigrationJob;
import io.migraflow.migration.domain.MigrationJobStatus;
import io.migraflow.migration.domain.MigrationSourceType;
import java.time.LocalDateTime;

/**
 * 이관 작업 조회/응답용 DTO.
 *
 * @param id               이관 작업 ID
 * @param projectId        소속 프로젝트 ID
 * @param name             작업명
 * @param targetEntityName 이관 대상 엔티티명
 * @param sourceType       AS-IS 데이터 수집 방식
 * @param status           현재 작업 상태
 * @param createdById      등록자 ID
 * @param submittedById    제출자 ID
 * @param submitDate       제출 일시
 * @param approvedById     승인자 ID
 * @param approveDate      승인 일시
 * @param rejectReason     반려 사유
 * @param createDate       생성 일시
 * @param updatedDate      수정 일시
 */
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

    /**
     * {@link MigrationJob} 엔티티를 응답 DTO로 변환한다.
     *
     * @param job 변환할 이관 작업 엔티티
     */
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
