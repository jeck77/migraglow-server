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
 * @param targetEntityName 이관 대상 엔티티명 (매핑 규칙을 묶는 키. 실제 TO-BE 테이블명과 다를 수 있다)
 * @param sourceType       AS-IS 데이터 수집 방식
 * @param sourceTableName  등록 시 선택한 AS-IS 테이블명 (sourceConfig에서 추출, 파싱 실패/미선택 시 null)
 * @param targetTableName  등록 시 선택한 TO-BE 테이블명 (targetConfig에서 추출, 파싱 실패/미선택 시 null)
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
        String sourceTableName,
        String targetTableName,
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
     * {@link MigrationJob} 엔티티와 별도로 추출한 AS-IS/TO-BE 테이블명을 응답 DTO로 변환한다.
     *
     * @param job             변환할 이관 작업 엔티티
     * @param sourceTableName sourceConfig에서 추출한 AS-IS 테이블명 (없으면 null)
     * @param targetTableName targetConfig에서 추출한 TO-BE 테이블명 (없으면 null)
     */
    public MigrationJobResponse(MigrationJob job, String sourceTableName, String targetTableName) {
        this(
                job.getId(),
                job.getProjectId(),
                job.getName(),
                job.getTargetEntityName(),
                job.getSourceType(),
                sourceTableName,
                targetTableName,
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
