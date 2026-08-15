package io.migraflow.migration.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * {@code VALUE_MAP} 타입 매핑 규칙에 쓰이는 AS-IS/TO-BE 값 한 쌍.
 *
 * @param sourceValue AS-IS 값
 * @param targetValue TO-BE 값
 */
public record MigrationMappingRuleValuePair(
        @NotBlank String sourceValue,
        @NotBlank String targetValue
) {
}
