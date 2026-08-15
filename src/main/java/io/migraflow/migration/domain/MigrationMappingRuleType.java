package io.migraflow.migration.domain;

import java.util.Arrays;

public enum MigrationMappingRuleType {

    DIRECT(0),
    VALUE_MAP(1),
    EXPRESSION(2),
    FIXED_VALUE(3);

    private final int code;

    MigrationMappingRuleType(int code) {
        this.code = code;
    }

    /**
     * DB 컬럼에 저장되는 정수 코드를 반환한다.
     *
     * @return 규칙 타입 코드
     */
    public int getCode() {
        return code;
    }

    /**
     * DB에 저장된 정수 코드를 대응하는 enum 상수로 변환한다.
     *
     * @param code 규칙 타입 코드
     * @return 해당 코드에 대응하는 {@link MigrationMappingRuleType}
     * @throws IllegalArgumentException 알 수 없는 코드인 경우
     */
    public static MigrationMappingRuleType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MigrationMappingRuleType code: " + code));
    }
}
