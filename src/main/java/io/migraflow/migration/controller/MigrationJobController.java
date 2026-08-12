package io.migraflow.migration.controller;

import io.migraflow.migration.dto.MigrationJobActionRequest;
import io.migraflow.migration.dto.MigrationJobApprovalHistoryResponse;
import io.migraflow.migration.dto.MigrationJobCreateRequest;
import io.migraflow.migration.dto.MigrationJobRejectRequest;
import io.migraflow.migration.dto.MigrationJobResponse;
import io.migraflow.migration.service.MigrationJobService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MigrationJobController {

    private final MigrationJobService migrationJobService;
    private final MessageSource messageSource;

    /**
     * 신규 이관 작업(MigrationJob)을 등록한다.
     *
     * @param projectId 이관 작업이 속한 프로젝트 ID
     * @param request   작업명, 대상 엔티티, 소스 타입 등 등록 요청 정보
     * @return 생성된 이관 작업 정보 (201 Created)
     */
    @PostMapping("/api/projects/{projectId}/migration-jobs")
    public ResponseEntity<MigrationJobResponse> createJob(@PathVariable Long projectId,
                                                            @Valid @RequestBody MigrationJobCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(migrationJobService.register(projectId, request));
    }

    /**
     * 특정 프로젝트에 속한 이관 작업 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @return 해당 프로젝트의 이관 작업 목록
     */
    @GetMapping("/api/projects/{projectId}/migration-jobs")
    public List<MigrationJobResponse> listJobs(@PathVariable Long projectId) {
        return migrationJobService.listByProject(projectId);
    }

    /**
     * 이관 작업 단건을 조회한다.
     *
     * @param jobId 이관 작업 ID
     * @return 이관 작업 상세 정보
     */
    @GetMapping("/api/migration-jobs/{jobId}")
    public MigrationJobResponse getJob(@PathVariable Long jobId) {
        return migrationJobService.getJob(jobId);
    }

    /**
     * 이관 작업의 제출/승인/반려 이력을 조회한다.
     *
     * @param jobId 이관 작업 ID
     * @return 승인 이력 목록
     */
    @GetMapping("/api/migration-jobs/{jobId}/approval-history")
    public List<MigrationJobApprovalHistoryResponse> getApprovalHistory(@PathVariable Long jobId) {
        return migrationJobService.getApprovalHistory(jobId);
    }

    /**
     * 이관 작업을 승인 대기 상태로 제출한다.
     *
     * @param jobId   이관 작업 ID
     * @param request 제출자 ID를 담은 요청
     * @return 제출 처리 후의 이관 작업 정보
     */
    @PostMapping("/api/migration-jobs/{jobId}/submit")
    public MigrationJobResponse submit(@PathVariable Long jobId, @Valid @RequestBody MigrationJobActionRequest request) {
        return migrationJobService.submit(jobId, request.actorId());
    }

    /**
     * 승인 대기 중인 이관 작업을 승인한다.
     *
     * @param jobId   이관 작업 ID
     * @param request 승인자 ID를 담은 요청
     * @return 승인 처리 후의 이관 작업 정보
     */
    @PostMapping("/api/migration-jobs/{jobId}/approve")
    public MigrationJobResponse approve(@PathVariable Long jobId, @Valid @RequestBody MigrationJobActionRequest request) {
        return migrationJobService.approve(jobId, request.actorId());
    }

    /**
     * 승인 대기 중인 이관 작업을 반려한다.
     *
     * @param jobId   이관 작업 ID
     * @param request 반려자 ID와 반려 사유를 담은 요청
     * @return 반려 처리 후의 이관 작업 정보
     */
    @PostMapping("/api/migration-jobs/{jobId}/reject")
    public MigrationJobResponse reject(@PathVariable Long jobId, @Valid @RequestBody MigrationJobRejectRequest request) {
        return migrationJobService.reject(jobId, request.actorId(), request.reason());
    }

    /**
     * 상태 전이 규칙 위반 등으로 발생한 {@link IllegalStateException}을 409 Conflict 응답으로 변환한다.
     * 예외 메시지는 {@code messages.properties}에 정의된 키로 취급하여 {@link MessageSource}로 실제 텍스트를 조회하며,
     * 등록되지 않은 키인 경우 원본 메시지를 그대로 사용한다.
     *
     * @param e 발생한 예외 (메시지는 messages.properties의 키)
     * @return 조회된 메시지를 담은 409 응답
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    /**
     * 대상 엔티티를 찾지 못해 발생한 {@link EntityNotFoundException}을 404 Not Found 응답으로 변환한다.
     *
     * @param e 발생한 예외
     * @return 예외 메시지를 담은 404 응답
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
