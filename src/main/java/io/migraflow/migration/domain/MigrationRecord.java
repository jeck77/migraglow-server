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

    /**
     * AS-IS에서 수집한 원본 레코드 한 건을 COLLECTED 상태로 생성한다.
     *
     * @param jobId     소속 이관 작업 ID
     * @param sourceKey AS-IS 원본 식별 키
     * @param rawData   수집된 원본 데이터(JSON)
     */
    public MigrationRecord(Long jobId, String sourceKey, String rawData) {
        this.jobId = jobId;
        this.sourceKey = sourceKey;
        this.rawData = rawData;
        this.status = MigrationRecordStatus.COLLECTED.getCode();
    }

    /**
     * 저장된 코드 값을 {@link MigrationRecordStatus} enum으로 변환하여 반환한다.
     *
     * @return 현재 레코드 상태
     */
    public MigrationRecordStatus getStatus() {
        return MigrationRecordStatus.fromCode(status);
    }

    /**
     * 매핑 규칙이 적용되어 매핑 완료 상태로 전환되었음을 표시한다.
     */
    public void markMapped() {
        this.status = MigrationRecordStatus.MAPPED.getCode();
    }

    /**
     * 담당자의 수동 보정이 반영되어 보정 완료 상태로 전환되었음을 표시한다.
     */
    public void markCorrected() {
        this.status = MigrationRecordStatus.CORRECTED.getCode();
    }

    /**
     * 이관 대상에서 제외되었음을 표시한다.
     */
    public void exclude() {
        this.status = MigrationRecordStatus.EXCLUDED.getCode();
    }

    /**
     * 배치 실행을 통해 TO-BE 반영이 성공했음을 표시하고 이전 에러 메시지를 초기화한다.
     */
    public void markExecuted() {
        this.status = MigrationRecordStatus.EXECUTED.getCode();
        this.errorMessage = null;
    }

    /**
     * 배치 실행 중 TO-BE 반영이 실패했음을 표시한다.
     *
     * @param errorMessage 실패 사유
     */
    public void markFailed(String errorMessage) {
        this.status = MigrationRecordStatus.FAILED.getCode();
        this.errorMessage = errorMessage;
    }
}
