package io.migraflow.migration.domain;

import java.util.Arrays;

public enum MigrationExecutionStatus {

    RUNNING(0),
    COMPLETED(1),
    FAILED(2);

    private final int code;

    MigrationExecutionStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MigrationExecutionStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MigrationExecutionStatus code: " + code));
    }
}
