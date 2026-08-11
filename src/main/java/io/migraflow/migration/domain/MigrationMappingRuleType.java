package io.migraflow.migration.domain;

import java.util.Arrays;

public enum MigrationMappingRuleType {

    DIRECT(0),
    VALUE_MAP(1),
    EXPRESSION(2);

    private final int code;

    MigrationMappingRuleType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MigrationMappingRuleType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MigrationMappingRuleType code: " + code));
    }
}
