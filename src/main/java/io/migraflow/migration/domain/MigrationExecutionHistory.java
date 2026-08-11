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
@Table(name = "MIGRATION_EXECUTION_HISTORY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MigrationExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "START_DATE", insertable = false, updatable = false)
    private LocalDateTime startDate;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;

    @Column(name = "SUCCESS_CNT", nullable = false)
    private Integer successCnt;

    @Column(name = "FAIL_CNT", nullable = false)
    private Integer failCnt;

    public MigrationExecutionHistory(Long jobId) {
        this.jobId = jobId;
        this.status = MigrationExecutionStatus.RUNNING.getCode();
        this.successCnt = 0;
        this.failCnt = 0;
    }

    public MigrationExecutionStatus getStatus() {
        return MigrationExecutionStatus.fromCode(status);
    }

    public void complete(int successCnt, int failCnt) {
        this.status = MigrationExecutionStatus.COMPLETED.getCode();
        this.successCnt = successCnt;
        this.failCnt = failCnt;
        this.endDate = LocalDateTime.now();
    }

    public void fail(int successCnt, int failCnt) {
        this.status = MigrationExecutionStatus.FAILED.getCode();
        this.successCnt = successCnt;
        this.failCnt = failCnt;
        this.endDate = LocalDateTime.now();
    }
}
