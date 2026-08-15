package io.migraflow.migration.controller;

import io.migraflow.migration.dto.MigrationMappingRuleCreateRequest;
import io.migraflow.migration.dto.MigrationMappingRuleResponse;
import io.migraflow.migration.service.MigrationMappingRuleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MigrationMappingRuleController {

    private final MigrationMappingRuleService migrationMappingRuleService;
    private final MessageSource messageSource;

    /**
     * 매핑 규칙을 등록한다. 같은 대상 필드의 기존 규칙이 있으면 교체된다.
     *
     * @param request 등록할 매핑 규칙 정보
     * @return 생성된 매핑 규칙 정보 (201 Created)
     */
    @PostMapping("/api/mapping-rules")
    public ResponseEntity<MigrationMappingRuleResponse> create(@Valid @RequestBody MigrationMappingRuleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(migrationMappingRuleService.create(request));
    }

    /**
     * 특정 대상 엔티티에 활성화된 매핑 규칙 목록을 조회한다.
     *
     * @param targetEntityName 대상 엔티티명
     * @return 활성 매핑 규칙 목록
     */
    @GetMapping("/api/mapping-rules")
    public List<MigrationMappingRuleResponse> list(@RequestParam String targetEntityName) {
        return migrationMappingRuleService.listByTargetEntity(targetEntityName);
    }

    /**
     * 매핑 규칙을 비활성화한다.
     *
     * @param ruleId 비활성화할 매핑 규칙 ID
     * @return 본문 없는 204 응답
     */
    @PostMapping("/api/mapping-rules/{ruleId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long ruleId) {
        migrationMappingRuleService.deactivate(ruleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * ruleType별 필수값 누락 등으로 발생한 {@link IllegalArgumentException}을 400 Bad Request 응답으로 변환한다.
     * 예외 메시지는 {@code messages.properties}에 정의된 키로 취급하여 {@link MessageSource}로 실제 텍스트를 조회하며,
     * 등록되지 않은 키인 경우 원본 메시지를 그대로 사용한다.
     *
     * @param e 발생한 예외 (메시지는 messages.properties의 키)
     * @return 조회된 메시지를 담은 400 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidRequest(IllegalArgumentException e) {
        String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    /**
     * 대상 매핑 규칙을 찾지 못해 발생한 {@link EntityNotFoundException}을 404 Not Found 응답으로 변환한다.
     *
     * @param e 발생한 예외
     * @return 예외 메시지를 담은 404 응답
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
