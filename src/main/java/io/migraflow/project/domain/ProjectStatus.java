package io.migraflow.project.domain;

import java.util.Arrays;

public enum ProjectStatus {

    ACTIVE(0),
    ARCHIVED(1);

    private final int code;

    ProjectStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ProjectStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ProjectStatus code: " + code));
    }
}
