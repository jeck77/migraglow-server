package io.migraflow.migration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "MIGRATION_JOB")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MigrationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "TARGET_ENTITY_NAME", nullable = false)
    private String targetEntityName;

    @Column(name = "SOURCE_TYPE", nullable = false)
    private Integer sourceType;

    @Column(name = "SOURCE_CONFIG", columnDefinition = "json")
    private String sourceConfig;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "CREATED_BY_ID", nullable = false)
    private Long createdById;

    @Column(name = "SUBMITTED_BY_ID")
    private Long submittedById;

    @Column(name = "SUBMIT_DATE")
    private LocalDateTime submitDate;

    @Column(name = "APPROVED_BY_ID")
    private Long approvedById;

    @Column(name = "APPROVE_DATE")
    private LocalDateTime approveDate;

    @Column(name = "REJECT_REASON")
    private String rejectReason;

    @Column(name = "EXECUTE_START_DATE")
    private LocalDateTime executeStartDate;

    @Column(name = "EXECUTE_END_DATE")
    private LocalDateTime executeEndDate;

    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "UPDATED_DATE", insertable = false, updatable = false)
    private LocalDateTime updatedDate;

    /**
     * DRAFT 상태의 신규 이관 작업을 생성한다.
     *
     * @param projectId        소속 프로젝트 ID
     * @param name             작업명
     * @param targetEntityName 이관 대상 엔티티명
     * @param sourceType       AS-IS 데이터 수집 방식
     * @param sourceConfig     소스 접속/조회 설정(JSON)
     * @param createdById      등록자 ID
     */
    public MigrationJob(Long projectId, String name, String targetEntityName, MigrationSourceType sourceType,
                         String sourceConfig, Long createdById) {
        this.projectId = projectId;
        this.name = name;
        this.targetEntityName = targetEntityName;
        this.sourceType = sourceType.getCode();
        this.sourceConfig = sourceConfig;
        this.status = MigrationJobStatus.DRAFT.getCode();
        this.createdById = createdById;
    }

    /**
     * 저장된 코드 값을 {@link MigrationSourceType} enum으로 변환하여 반환한다.
     *
     * @return AS-IS 데이터 수집 방식
     */
    public MigrationSourceType getSourceType() {
        return MigrationSourceType.fromCode(sourceType);
    }

    /**
     * 저장된 코드 값을 {@link MigrationJobStatus} enum으로 변환하여 반환한다.
     *
     * @return 현재 이관 작업 상태
     */
    public MigrationJobStatus getStatus() {
        return MigrationJobStatus.fromCode(status);
    }

    /**
     * 이관 작업을 승인 대기 상태로 제출한다. DRAFT 또는 REJECTED 상태에서만 가능하다.
     *
     * @param submittedById 제출자 ID
     * @throws IllegalStateException DRAFT/REJECTED 상태가 아닌 경우 (메시지는 {@code messages.properties}의
     *                                {@code migrationJob.submit.invalidState} 키)
     */
    public void submit(Long submittedById) {
        MigrationJobStatus current = getStatus();
        if (current != MigrationJobStatus.DRAFT && current != MigrationJobStatus.REJECTED) {
            throw new IllegalStateException("migrationJob.submit.invalidState");
        }
        this.status = MigrationJobStatus.SUBMITTED.getCode();
        this.submittedById = submittedById;
        this.submitDate = LocalDateTime.now();
    }

    /**
     * 승인 대기 중인 이관 작업을 승인한다. SUBMITTED 상태에서만 가능하다.
     *
     * @param approvedById 승인자 ID
     * @throws IllegalStateException SUBMITTED 상태가 아닌 경우 (메시지는 {@code messages.properties}의
     *                                {@code migrationJob.approve.invalidState} 키)
     */
    public void approve(Long approvedById) {
        if (getStatus() != MigrationJobStatus.SUBMITTED) {
            throw new IllegalStateException("migrationJob.approve.invalidState");
        }
        this.status = MigrationJobStatus.APPROVED.getCode();
        this.approvedById = approvedById;
        this.approveDate = LocalDateTime.now();
    }

    /**
     * 승인 대기 중인 이관 작업을 반려한다. SUBMITTED 상태에서만 가능하며, 반려 후 다시 보정 단계로 돌아갈 수 있다.
     *
     * @param rejectReason 반려 사유
     * @throws IllegalStateException SUBMITTED 상태가 아닌 경우 (메시지는 {@code messages.properties}의
     *                                {@code migrationJob.reject.invalidState} 키)
     */
    public void reject(String rejectReason) {
        if (getStatus() != MigrationJobStatus.SUBMITTED) {
            throw new IllegalStateException("migrationJob.reject.invalidState");
        }
        this.status = MigrationJobStatus.REJECTED.getCode();
        this.rejectReason = rejectReason;
    }

    /**
     * 승인된 작업의 배치 실행을 시작한다. APPROVED 상태에서만 가능하다.
     *
     * @throws IllegalStateException APPROVED 상태가 아닌 경우 (메시지는 {@code messages.properties}의
     *                                {@code migrationJob.execute.invalidState} 키)
     */
    public void startExecution() {
        if (getStatus() != MigrationJobStatus.APPROVED) {
            throw new IllegalStateException("migrationJob.execute.invalidState");
        }
        this.status = MigrationJobStatus.EXECUTING.getCode();
        this.executeStartDate = LocalDateTime.now();
    }

    /**
     * 배치 실행을 성공적으로 완료 처리한다.
     */
    public void completeExecution() {
        this.status = MigrationJobStatus.COMPLETED.getCode();
        this.executeEndDate = LocalDateTime.now();
    }

    /**
     * 배치 실행을 실패 처리한다.
     */
    public void failExecution() {
        this.status = MigrationJobStatus.FAILED.getCode();
        this.executeEndDate = LocalDateTime.now();
    }
}
