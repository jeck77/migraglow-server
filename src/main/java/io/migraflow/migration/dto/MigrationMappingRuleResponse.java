package io.migraflow.migration.dto;

import io.migraflow.migration.domain.MigrationMappingRule;
import io.migraflow.migration.domain.MigrationMappingRuleType;
import io.migraflow.migration.domain.MigrationMappingRuleValue;
import java.util.List;

/**
 * 매핑 규칙 조회/응답용 DTO.
 *
 * @param id               매핑 규칙 ID
 * @param targetEntityName 매핑 대상 TO-BE 엔티티/테이블명
 * @param sourceFieldName  AS-IS 컬럼명 ({@code FIXED_VALUE} 타입이면 null)
 * @param targetFieldName  TO-BE 컬럼명
 * @param ruleType         매핑/변환 규칙 타입
 * @param expression       변환식 또는 고정 보정값
 * @param useYn            사용 여부
 * @param valueMap         {@code VALUE_MAP} 타입일 때의 AS-IS→TO-BE 값 쌍 목록
 */
public record MigrationMappingRuleResponse(
        Long id,
        String targetEntityName,
        String sourceFieldName,
        String targetFieldName,
        MigrationMappingRuleType ruleType,
        String expression,
        Boolean useYn,
        List<MigrationMappingRuleValuePair> valueMap
) {

    /**
     * {@link MigrationMappingRule} 엔티티와 그에 속한 값 매핑 목록을 응답 DTO로 변환한다.
     *
     * @param rule   변환할 매핑 규칙 엔티티
     * @param values 해당 규칙의 {@code VALUE_MAP} 값 쌍 목록 (없으면 빈 목록)
     */
    public MigrationMappingRuleResponse(MigrationMappingRule rule, List<MigrationMappingRuleValue> values) {
        this(
                rule.getId(),
                rule.getTargetEntityName(),
                rule.getSourceFieldName(),
                rule.getTargetFieldName(),
                rule.getRuleType(),
                rule.getExpression(),
                rule.getUseYn(),
                values.stream().map(v -> new MigrationMappingRuleValuePair(v.getSourceValue(), v.getTargetValue())).toList()
        );
    }
}
