package io.migraflow.migration.domain;

import java.util.Arrays;

public enum MigrationSourceType {

    DB(0),
    API(1),
    FILE(2);

    private final int code;

    MigrationSourceType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MigrationSourceType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MigrationSourceType code: " + code));
    }
}
