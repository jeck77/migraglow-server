package io.migraflow.migration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "MIGRATION_RECORD_FIELD")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MigrationRecordField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "RECORD_ID", nullable = false)
    private Long recordId;

    @Column(name = "RULE_ID")
    private Long ruleId;

    @Column(name = "TARGET_FIELD_NAME", nullable = false)
    private String targetFieldName;

    @Column(name = "RAW_VALUE")
    private String rawValue;

    @Column(name = "MAPPED_VALUE")
    private String mappedValue;

    @Column(name = "MANUAL_VALUE")
    private String manualValue;

    @Column(name = "FINAL_VALUE")
    private String finalValue;

    @Column(name = "IS_MANUAL_YN", nullable = false)
    private Boolean manualYn;

    /**
     * 레코드의 필드 단위 값(원본값/매핑 후 값/최종값)을 매핑 규칙 적용 직후 상태로 생성한다.
     *
     * @param recordId        소속 레코드 ID
     * @param ruleId          적용된 매핑 규칙 ID (매핑 규칙이 없으면 null)
     * @param targetFieldName TO-BE 필드명
     * @param rawValue        AS-IS 원본값
     * @param mappedValue     매핑 규칙 적용 후 값
     */
    public MigrationRecordField(Long recordId, Long ruleId, String targetFieldName, String rawValue, String mappedValue) {
        this.recordId = recordId;
        this.ruleId = ruleId;
        this.targetFieldName = targetFieldName;
        this.rawValue = rawValue;
        this.mappedValue = mappedValue;
        this.finalValue = mappedValue;
        this.manualYn = false;
    }

    /**
     * 담당자가 필드 값을 수동으로 교정하고, 이를 최종값으로 반영한다.
     *
     * @param manualValue 담당자가 입력한 수동 보정값
     */
    public void correctManually(String manualValue) {
        this.manualValue = manualValue;
        this.finalValue = manualValue;
        this.manualYn = true;
    }

    /**
     * 수동 보정을 취소하고, 최종값을 매핑 규칙 적용 값으로 되돌린다.
     */
    public void clearManualCorrection() {
        this.manualValue = null;
        this.finalValue = this.mappedValue;
        this.manualYn = false;
    }
}
