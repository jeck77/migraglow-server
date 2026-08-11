package io.migraflow.migration.service;

import io.migraflow.migration.domain.MigrationJob;
import io.migraflow.migration.domain.MigrationJobActionType;
import io.migraflow.migration.domain.MigrationJobApprovalHistory;
import io.migraflow.migration.dto.MigrationJobApprovalHistoryResponse;
import io.migraflow.migration.dto.MigrationJobCreateRequest;
import io.migraflow.migration.dto.MigrationJobResponse;
import io.migraflow.migration.repository.MigrationJobApprovalHistoryRepository;
import io.migraflow.migration.repository.MigrationJobRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MigrationJobService {

    private final MigrationJobRepository migrationJobRepository;
    private final MigrationJobApprovalHistoryRepository migrationJobApprovalHistoryRepository;

    public MigrationJobResponse register(Long projectId, MigrationJobCreateRequest request) {
        MigrationJob job = new MigrationJob(
                projectId,
                request.name(),
                request.targetEntityName(),
                request.sourceType(),
                request.sourceConfig(),
                request.createdById()
        );
        migrationJobRepository.save(job);
        return new MigrationJobResponse(job);
    }

    public MigrationJobResponse submit(Long jobId, Long actorId) {
        MigrationJob job = getJobOrThrow(jobId);
        job.submit(actorId);
        migrationJobApprovalHistoryRepository.save(
                new MigrationJobApprovalHistory(jobId, MigrationJobActionType.SUBMIT, actorId, null));
        return new MigrationJobResponse(job);
    }

    public MigrationJobResponse approve(Long jobId, Long actorId) {
        MigrationJob job = getJobOrThrow(jobId);
        job.approve(actorId);
        migrationJobApprovalHistoryRepository.save(
                new MigrationJobApprovalHistory(jobId, MigrationJobActionType.APPROVE, actorId, null));
        return new MigrationJobResponse(job);
    }

    public MigrationJobResponse reject(Long jobId, Long actorId, String reason) {
        MigrationJob job = getJobOrThrow(jobId);
        job.reject(reason);
        migrationJobApprovalHistoryRepository.save(
                new MigrationJobApprovalHistory(jobId, MigrationJobActionType.REJECT, actorId, reason));
        return new MigrationJobResponse(job);
    }

    @Transactional(readOnly = true)
    public MigrationJobResponse getJob(Long jobId) {
        return new MigrationJobResponse(getJobOrThrow(jobId));
    }

    @Transactional(readOnly = true)
    public List<MigrationJobResponse> listByProject(Long projectId) {
        return migrationJobRepository.findByProjectId(projectId).stream()
                .map(MigrationJobResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MigrationJobApprovalHistoryResponse> getApprovalHistory(Long jobId) {
        return migrationJobApprovalHistoryRepository.findByJobIdOrderByActionDateDesc(jobId).stream()
                .map(MigrationJobApprovalHistoryResponse::new)
                .toList();
    }

    private MigrationJob getJobOrThrow(Long jobId) {
        return migrationJobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("MigrationJob not found: " + jobId));
    }
}
