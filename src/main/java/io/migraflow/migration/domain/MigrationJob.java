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

    public MigrationSourceType getSourceType() {
        return MigrationSourceType.fromCode(sourceType);
    }

    public MigrationJobStatus getStatus() {
        return MigrationJobStatus.fromCode(status);
    }

    public void submit(Long submittedById) {
        MigrationJobStatus current = getStatus();
        if (current != MigrationJobStatus.DRAFT && current != MigrationJobStatus.REJECTED) {
            throw new IllegalStateException("등록/보정 또는 반려 상태에서만 제출할 수 있습니다.");
        }
        this.status = MigrationJobStatus.SUBMITTED.getCode();
        this.submittedById = submittedById;
        this.submitDate = LocalDateTime.now();
    }

    public void approve(Long approvedById) {
        if (getStatus() != MigrationJobStatus.SUBMITTED) {
            throw new IllegalStateException("승인 대기 상태에서만 승인할 수 있습니다.");
        }
        this.status = MigrationJobStatus.APPROVED.getCode();
        this.approvedById = approvedById;
        this.approveDate = LocalDateTime.now();
    }

    public void reject(String rejectReason) {
        if (getStatus() != MigrationJobStatus.SUBMITTED) {
            throw new IllegalStateException("승인 대기 상태에서만 반려할 수 있습니다.");
        }
        this.status = MigrationJobStatus.REJECTED.getCode();
        this.rejectReason = rejectReason;
    }

    public void startExecution() {
        if (getStatus() != MigrationJobStatus.APPROVED) {
            throw new IllegalStateException("승인된 작업만 실행할 수 있습니다.");
        }
        this.status = MigrationJobStatus.EXECUTING.getCode();
        this.executeStartDate = LocalDateTime.now();
    }

    public void completeExecution() {
        this.status = MigrationJobStatus.COMPLETED.getCode();
        this.executeEndDate = LocalDateTime.now();
    }

    public void failExecution() {
        this.status = MigrationJobStatus.FAILED.getCode();
        this.executeEndDate = LocalDateTime.now();
    }
}
