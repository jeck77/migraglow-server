package io.migraflow.migration.domain;

import java.util.Arrays;

public enum MigrationJobStatus {

    DRAFT(0),
    SUBMITTED(1),
    APPROVED(2),
    REJECTED(3),
    EXECUTING(4),
    COMPLETED(5),
    FAILED(6);

    private final int code;

    MigrationJobStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MigrationJobStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MigrationJobStatus code: " + code));
    }
}
