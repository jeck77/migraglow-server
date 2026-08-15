package io.migraflow.migration.controller;

import io.migraflow.migration.dto.ColumnInfoResponse;
import io.migraflow.migration.dto.DbConnectionRequest;
import io.migraflow.migration.dto.MigrationSchemaColumnsRequest;
import io.migraflow.migration.service.MigrationSchemaConnectionException;
import io.migraflow.migration.service.MigrationSchemaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MigrationSchemaController {

    private final MigrationSchemaService migrationSchemaService;
    private final MessageSource messageSource;

    /**
     * AS-IS/TO-BE DB에 접속해 조회 가능한 테이블 목록을 반환한다.
     *
     * @param request DB 접속 정보
     * @return 테이블명 목록
     */
    @PostMapping("/api/migration-schema/tables")
    public List<String> listTables(@Valid @RequestBody DbConnectionRequest request) {
        return migrationSchemaService.listTables(request);
    }

    /**
     * AS-IS/TO-BE DB의 특정 테이블에 속한 컬럼 목록을 반환한다.
     *
     * @param request DB 접속 정보와 조회할 테이블명
     * @return 컬럼 메타데이터 목록
     */
    @PostMapping("/api/migration-schema/columns")
    public List<ColumnInfoResponse> listColumns(@Valid @RequestBody MigrationSchemaColumnsRequest request) {
        return migrationSchemaService.listColumns(request.connection(), request.tableName());
    }

    /**
     * DB 접속 실패로 발생한 {@link MigrationSchemaConnectionException}을 502 Bad Gateway 응답으로 변환한다.
     * 예외 메시지는 {@code messages.properties}에 정의된 키로 취급하여 {@link MessageSource}로 실제 텍스트를 조회하며,
     * 등록되지 않은 키인 경우 원본 메시지를 그대로 사용한다.
     *
     * @param e 발생한 예외 (메시지는 messages.properties의 키)
     * @return 조회된 메시지를 담은 502 응답
     */
    @ExceptionHandler(MigrationSchemaConnectionException.class)
    public ResponseEntity<String> handleConnectionFailure(MigrationSchemaConnectionException e) {
        String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(message);
    }
}
