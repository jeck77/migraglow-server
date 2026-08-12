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
@Table(name = "MIGRATION_MAPPING_RULE_VALUE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MigrationMappingRuleValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "RULE_ID", nullable = false)
    private Long ruleId;

    @Column(name = "SOURCE_VALUE", nullable = false)
    private String sourceValue;

    @Column(name = "TARGET_VALUE", nullable = false)
    private String targetValue;

    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    private LocalDateTime createDate;

    /**
     * VALUE_MAP 타입 매핑 규칙에서 사용할 AS-IS/TO-BE 값 쌍을 생성한다.
     *
     * @param ruleId      대상 매핑 규칙 ID
     * @param sourceValue AS-IS 값
     * @param targetValue TO-BE 값
     */
    public MigrationMappingRuleValue(Long ruleId, String sourceValue, String targetValue) {
        this.ruleId = ruleId;
        this.sourceValue = sourceValue;
        this.targetValue = targetValue;
    }
}
