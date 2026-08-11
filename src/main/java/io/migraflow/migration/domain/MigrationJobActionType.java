package io.migraflow.migration.domain;

import java.util.Arrays;

public enum MigrationJobActionType {

    SUBMIT(0),
    APPROVE(1),
    REJECT(2);

    private final int code;

    MigrationJobActionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MigrationJobActionType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MigrationJobActionType code: " + code));
    }
}
