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

    @Column(name = "SOURCE_FIELD_NAME")
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

    /**
     * AS-IS 필드를 TO-BE 필드로 변환하는 매핑 규칙을 활성 상태로 생성한다.
     *
     * @param targetEntityName 규칙이 적용될 대상 엔티티명
     * @param sourceFieldName  AS-IS 필드명. {@code ruleType}이 {@code FIXED_VALUE}면 소스 없이 고정값만 쓰므로 null 허용
     * @param targetFieldName  TO-BE 필드명
     * @param ruleType         매핑/변환 규칙 타입
     * @param expression       변환 표현식. {@code EXPRESSION} 타입은 변환식, {@code FIXED_VALUE} 타입은 고정값 그 자체를 담는다
     */
    public MigrationMappingRule(String targetEntityName, String sourceFieldName, String targetFieldName,
                                 MigrationMappingRuleType ruleType, String expression) {
        this.targetEntityName = targetEntityName;
        this.sourceFieldName = sourceFieldName;
        this.targetFieldName = targetFieldName;
        this.ruleType = ruleType.getCode();
        this.expression = expression;
        this.useYn = true;
    }

    /**
     * 저장된 코드 값을 {@link MigrationMappingRuleType} enum으로 변환하여 반환한다.
     *
     * @return 매핑/변환 규칙 타입
     */
    public MigrationMappingRuleType getRuleType() {
        return MigrationMappingRuleType.fromCode(ruleType);
    }

    /**
     * 변환 표현식을 수정한다.
     *
     * @param expression 새 변환 표현식
     */
    public void changeExpression(String expression) {
        this.expression = expression;
    }

    /**
     * 매핑 규칙을 활성화하여 이후 매핑 적용 시 사용되도록 한다.
     */
    public void activate() {
        this.useYn = true;
    }

    /**
     * 매핑 규칙을 비활성화하여 이후 매핑 적용 시 사용되지 않도록 한다.
     */
    public void deactivate() {
        this.useYn = false;
    }
}
