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
@Table(name = "MIGRATION_RECORD")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MigrationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Column(name = "SOURCE_KEY", nullable = false)
    private String sourceKey;

    @Column(name = "RAW_DATA", nullable = false, columnDefinition = "json")
    private String rawData;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "UPDATED_DATE", insertable = false, updatable = false)
    private LocalDateTime updatedDate;

    public MigrationRecord(Long jobId, String sourceKey, String rawData) {
        this.jobId = jobId;
        this.sourceKey = sourceKey;
        this.rawData = rawData;
        this.status = MigrationRecordStatus.COLLECTED.getCode();
    }

    public MigrationRecordStatus getStatus() {
        return MigrationRecordStatus.fromCode(status);
    }

    public void markMapped() {
        this.status = MigrationRecordStatus.MAPPED.getCode();
    }

    public void markCorrected() {
        this.status = MigrationRecordStatus.CORRECTED.getCode();
    }

    public void exclude() {
        this.status = MigrationRecordStatus.EXCLUDED.getCode();
    }

    public void markExecuted() {
        this.status = MigrationRecordStatus.EXECUTED.getCode();
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = MigrationRecordStatus.FAILED.getCode();
        this.errorMessage = errorMessage;
    }
}
