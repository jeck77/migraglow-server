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

    /**
     * 신규 이관 작업을 DRAFT 상태로 등록한다.
     *
     * @param projectId 소속 프로젝트 ID
     * @param request   작업명, 대상 엔티티, 소스 타입 등 등록 요청 정보
     * @return 생성된 이관 작업 정보
     */
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

    /**
     * 이관 작업을 승인 대기 상태로 제출하고 제출 이력을 남긴다.
     *
     * @param jobId   이관 작업 ID
     * @param actorId 제출자 ID
     * @return 제출 처리 후의 이관 작업 정보
     * @throws EntityNotFoundException 이관 작업이 존재하지 않는 경우
     */
    public MigrationJobResponse submit(Long jobId, Long actorId) {
        MigrationJob job = getJobOrThrow(jobId);
        job.submit(actorId);
        migrationJobApprovalHistoryRepository.save(
                new MigrationJobApprovalHistory(jobId, MigrationJobActionType.SUBMIT, actorId, null));
        return new MigrationJobResponse(job);
    }

    /**
     * 승인 대기 중인 이관 작업을 승인하고 승인 이력을 남긴다.
     *
     * @param jobId   이관 작업 ID
     * @param actorId 승인자 ID
     * @return 승인 처리 후의 이관 작업 정보
     * @throws EntityNotFoundException 이관 작업이 존재하지 않는 경우
     */
    public MigrationJobResponse approve(Long jobId, Long actorId) {
        MigrationJob job = getJobOrThrow(jobId);
        job.approve(actorId);
        migrationJobApprovalHistoryRepository.save(
                new MigrationJobApprovalHistory(jobId, MigrationJobActionType.APPROVE, actorId, null));
        return new MigrationJobResponse(job);
    }

    /**
     * 승인 대기 중인 이관 작업을 반려하고 반려 이력을 남긴다.
     *
     * @param jobId   이관 작업 ID
     * @param actorId 반려자 ID
     * @param reason  반려 사유
     * @return 반려 처리 후의 이관 작업 정보
     * @throws EntityNotFoundException 이관 작업이 존재하지 않는 경우
     */
    public MigrationJobResponse reject(Long jobId, Long actorId, String reason) {
        MigrationJob job = getJobOrThrow(jobId);
        job.reject(reason);
        migrationJobApprovalHistoryRepository.save(
                new MigrationJobApprovalHistory(jobId, MigrationJobActionType.REJECT, actorId, reason));
        return new MigrationJobResponse(job);
    }

    /**
     * 이관 작업 단건을 조회한다.
     *
     * @param jobId 이관 작업 ID
     * @return 이관 작업 상세 정보
     * @throws EntityNotFoundException 이관 작업이 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public MigrationJobResponse getJob(Long jobId) {
        return new MigrationJobResponse(getJobOrThrow(jobId));
    }

    /**
     * 특정 프로젝트에 속한 이관 작업 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @return 해당 프로젝트의 이관 작업 목록
     */
    @Transactional(readOnly = true)
    public List<MigrationJobResponse> listByProject(Long projectId) {
        return migrationJobRepository.findByProjectId(projectId).stream()
                .map(MigrationJobResponse::new)
                .toList();
    }

    /**
     * 이관 작업의 제출/승인/반려 이력을 최신순으로 조회한다.
     *
     * @param jobId 이관 작업 ID
     * @return 승인 이력 목록
     */
    @Transactional(readOnly = true)
    public List<MigrationJobApprovalHistoryResponse> getApprovalHistory(Long jobId) {
        return migrationJobApprovalHistoryRepository.findByJobIdOrderByActionDateDesc(jobId).stream()
                .map(MigrationJobApprovalHistoryResponse::new)
                .toList();
    }

    /**
     * 이관 작업을 ID로 조회하고, 존재하지 않으면 예외를 던진다.
     *
     * @param jobId 이관 작업 ID
     * @return 조회된 이관 작업 엔티티
     * @throws EntityNotFoundException 이관 작업이 존재하지 않는 경우
     */
    private MigrationJob getJobOrThrow(Long jobId) {
        return migrationJobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("MigrationJob not found: " + jobId));
    }
}
