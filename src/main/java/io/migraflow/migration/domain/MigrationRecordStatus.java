package io.migraflow.migration.domain;

import java.util.Arrays;

public enum MigrationRecordStatus {

    COLLECTED(0),
    MAPPED(1),
    CORRECTED(2),
    EXCLUDED(3),
    EXECUTED(4),
    FAILED(5);

    private final int code;

    MigrationRecordStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MigrationRecordStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MigrationRecordStatus code: " + code));
    }
}
