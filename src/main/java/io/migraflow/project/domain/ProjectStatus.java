package io.migraflow.project.domain;

import java.util.Arrays;

public enum ProjectStatus {

    ACTIVE(0),
    ARCHIVED(1);

    private final int code;

    ProjectStatus(int code) {
        this.code = code;
    }

    /**
     * DB 컬럼에 저장되는 정수 코드를 반환한다.
     *
     * @return 상태 코드
     */
    public int getCode() {
        return code;
    }

    /**
     * DB에 저장된 정수 코드를 대응하는 enum 상수로 변환한다.
     *
     * @param code 상태 코드
     * @return 해당 코드에 대응하는 {@link ProjectStatus}
     * @throws IllegalArgumentException 알 수 없는 코드인 경우
     */
    public static ProjectStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ProjectStatus code: " + code));
    }
}
