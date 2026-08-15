package io.migraflow.migration.service;

import java.sql.SQLException;

/**
 * AS-IS/TO-BE DB 접속에 실패했을 때 발생하는 예외. 메시지는 {@code messages.properties}의 키로 취급된다.
 */
public class MigrationSchemaConnectionException extends RuntimeException {

    /**
     * 원인이 된 {@link SQLException}을 포함하여 접속 실패 예외를 생성한다.
     *
     * @param cause 접속 시도 중 발생한 SQL 예외
     */
    public MigrationSchemaConnectionException(SQLException cause) {
        super("migrationSchema.connect.failed", cause);
    }
}
