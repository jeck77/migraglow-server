package io.migraflow.migration.dto;

import io.migraflow.migration.domain.MigrationMappingRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * AS-IS 컬럼과 TO-BE 컬럼을 잇는 매핑 규칙 등록/교체 요청. 이미 같은 {@code targetEntityName}+{@code targetFieldName}
 * 조합의 규칙이 있으면 교체된다 (기존 규칙과 그 값 매핑은 삭제 후 새로 생성).
 *
 * @param targetEntityName 매핑 대상 TO-BE 엔티티/테이블명
 * @param sourceFieldName  AS-IS 컬럼명. {@code ruleType}이 {@code FIXED_VALUE}면 사용하지 않는다
 * @param targetFieldName  TO-BE 컬럼명. TO-BE의 기본키(PK) 컬럼은 시퀀스/auto_increment로 채번되므로 지정할 수 없다
 * @param ruleType         매핑/변환 규칙 타입
 * @param expression       {@code EXPRESSION} 타입이면 변환식, {@code FIXED_VALUE} 타입이면 고정 보정값 그 자체
 * @param valueMap         {@code VALUE_MAP} 타입일 때의 AS-IS→TO-BE 값 쌍 목록
 */
public record MigrationMappingRuleCreateRequest(
        @NotBlank String targetEntityName,
        String sourceFieldName,
        @NotBlank String targetFieldName,
        @NotNull MigrationMappingRuleType ruleType,
        String expression,
        List<MigrationMappingRuleValuePair> valueMap
) {
}
