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
@Table(name = "MIGRATION_MAPPING_RULE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MigrationMappingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TARGET_ENTITY_NAME", nullable = false)
    private String targetEntityName;

    @Column(name = "SOURCE_FIELD_NAME", nullable = false)
    private String sourceFieldName;

    @Column(name = "TARGET_FIELD_NAME", nullable = false)
    private String targetFieldName;

    @Column(name = "RULE_TYPE", nullable = false)
    private Integer ruleType;

    @Column(name = "EXPRESSION")
    private String expression;

    @Column(name = "USE_YN", nullable = false)
    private Boolean useYn;

    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "UPDATED_DATE", insertable = false, updatable = false)
    private LocalDateTime updatedDate;

    public MigrationMappingRule(String targetEntityName, String sourceFieldName, String targetFieldName,
                                 MigrationMappingRuleType ruleType, String expression) {
        this.targetEntityName = targetEntityName;
        this.sourceFieldName = sourceFieldName;
        this.targetFieldName = targetFieldName;
        this.ruleType = ruleType.getCode();
        this.expression = expression;
        this.useYn = true;
    }

    public MigrationMappingRuleType getRuleType() {
        return MigrationMappingRuleType.fromCode(ruleType);
    }

    public void changeExpression(String expression) {
        this.expression = expression;
    }

    public void activate() {
        this.useYn = true;
    }

    public void deactivate() {
        this.useYn = false;
    }
}
