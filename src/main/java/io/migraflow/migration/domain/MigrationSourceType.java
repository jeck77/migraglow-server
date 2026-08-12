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

    /**
     * DB 컬럼에 저장되는 정수 코드를 반환한다.
     *
     * @return 소스 타입 코드
     */
    public int getCode() {
        return code;
    }

    /**
     * DB에 저장된 정수 코드를 대응하는 enum 상수로 변환한다.
     *
     * @param code 소스 타입 코드
     * @return 해당 코드에 대응하는 {@link MigrationSourceType}
     * @throws IllegalArgumentException 알 수 없는 코드인 경우
     */
    public static MigrationSourceType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MigrationSourceType code: " + code));
    }
}
