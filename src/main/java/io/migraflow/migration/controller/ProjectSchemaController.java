package io.migraflow.migration.controller;

import io.migraflow.migration.dto.ColumnInfoResponse;
import io.migraflow.migration.service.MigrationSchemaConnectionException;
import io.migraflow.migration.service.ProjectSchemaService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProjectSchemaController {

    private final ProjectSchemaService projectSchemaService;
    private final MessageSource messageSource;

    /**
     * 프로젝트에 등록된 AS-IS DB의 테이블 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @return 테이블명 목록
     */
    @GetMapping("/api/projects/{projectId}/source-tables")
    public List<String> listSourceTables(@PathVariable Long projectId) {
        return projectSchemaService.listSourceTables(projectId);
    }

    /**
     * 프로젝트에 등록된 TO-BE DB의 테이블 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @return 테이블명 목록
     */
    @GetMapping("/api/projects/{projectId}/target-tables")
    public List<String> listTargetTables(@PathVariable Long projectId) {
        return projectSchemaService.listTargetTables(projectId);
    }

    /**
     * 프로젝트에 등록된 AS-IS DB의 특정 테이블 컬럼 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @param tableName 컬럼 목록을 조회할 테이블명
     * @return 컬럼 메타데이터 목록
     */
    @GetMapping("/api/projects/{projectId}/source-columns")
    public List<ColumnInfoResponse> listSourceColumns(@PathVariable Long projectId, @RequestParam String tableName) {
        return projectSchemaService.listSourceColumns(projectId, tableName);
    }

    /**
     * 프로젝트에 등록된 TO-BE DB의 특정 테이블 컬럼 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @param tableName 컬럼 목록을 조회할 테이블명
     * @return 컬럼 메타데이터 목록 (기본키/외래키 정보 포함)
     */
    @GetMapping("/api/projects/{projectId}/target-columns")
    public List<ColumnInfoResponse> listTargetColumns(@PathVariable Long projectId, @RequestParam String tableName) {
        return projectSchemaService.listTargetColumns(projectId, tableName);
    }

    /**
     * 접속 정보 미등록/형식 오류 등으로 발생한 {@link IllegalStateException}을 409 Conflict 응답으로 변환한다.
     * 예외 메시지는 {@code messages.properties}에 정의된 키로 취급하여 {@link MessageSource}로 실제 텍스트를 조회한다.
     *
     * @param e 발생한 예외 (메시지는 messages.properties의 키)
     * @return 조회된 메시지를 담은 409 응답
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleInvalidConfig(IllegalStateException e) {
        String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    /**
     * DB 접속 실패로 발생한 {@link MigrationSchemaConnectionException}을 502 Bad Gateway 응답으로 변환한다.
     *
     * @param e 발생한 예외
     * @return 조회된 메시지를 담은 502 응답
     */
    @ExceptionHandler(MigrationSchemaConnectionException.class)
    public ResponseEntity<String> handleConnectionFailure(MigrationSchemaConnectionException e) {
        String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(message);
    }

    /**
     * 대상 프로젝트를 찾지 못해 발생한 {@link EntityNotFoundException}을 404 Not Found 응답으로 변환한다.
     *
     * @param e 발생한 예외
     * @return 예외 메시지를 담은 404 응답
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
