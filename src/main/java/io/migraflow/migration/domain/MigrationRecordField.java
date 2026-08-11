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

    public MigrationRecordField(Long recordId, Long ruleId, String targetFieldName, String rawValue, String mappedValue) {
        this.recordId = recordId;
        this.ruleId = ruleId;
        this.targetFieldName = targetFieldName;
        this.rawValue = rawValue;
        this.mappedValue = mappedValue;
        this.finalValue = mappedValue;
        this.manualYn = false;
    }

    public void correctManually(String manualValue) {
        this.manualValue = manualValue;
        this.finalValue = manualValue;
        this.manualYn = true;
    }

    public void clearManualCorrection() {
        this.manualValue = null;
        this.finalValue = this.mappedValue;
        this.manualYn = false;
    }
}
