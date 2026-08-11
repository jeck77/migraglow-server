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

    @PostMapping("/api/projects/{projectId}/migration-jobs")
    public ResponseEntity<MigrationJobResponse> createJob(@PathVariable Long projectId,
                                                            @Valid @RequestBody MigrationJobCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(migrationJobService.register(projectId, request));
    }

    @GetMapping("/api/projects/{projectId}/migration-jobs")
    public List<MigrationJobResponse> listJobs(@PathVariable Long projectId) {
        return migrationJobService.listByProject(projectId);
    }

    @GetMapping("/api/migration-jobs/{jobId}")
    public MigrationJobResponse getJob(@PathVariable Long jobId) {
        return migrationJobService.getJob(jobId);
    }

    @GetMapping("/api/migration-jobs/{jobId}/approval-history")
    public List<MigrationJobApprovalHistoryResponse> getApprovalHistory(@PathVariable Long jobId) {
        return migrationJobService.getApprovalHistory(jobId);
    }

    @PostMapping("/api/migration-jobs/{jobId}/submit")
    public MigrationJobResponse submit(@PathVariable Long jobId, @Valid @RequestBody MigrationJobActionRequest request) {
        return migrationJobService.submit(jobId, request.actorId());
    }

    @PostMapping("/api/migration-jobs/{jobId}/approve")
    public MigrationJobResponse approve(@PathVariable Long jobId, @Valid @RequestBody MigrationJobActionRequest request) {
        return migrationJobService.approve(jobId, request.actorId());
    }

    @PostMapping("/api/migration-jobs/{jobId}/reject")
    public MigrationJobResponse reject(@PathVariable Long jobId, @Valid @RequestBody MigrationJobRejectRequest request) {
        return migrationJobService.reject(jobId, request.actorId(), request.reason());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
