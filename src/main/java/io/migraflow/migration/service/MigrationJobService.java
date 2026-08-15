package io.migraflow.migration.service;

import io.migraflow.migration.domain.MigrationJob;
import io.migraflow.migration.domain.MigrationJobActionType;
import io.migraflow.migration.domain.MigrationJobApprovalHistory;
import io.migraflow.migration.domain.MigrationSourceType;
import io.migraflow.migration.dto.ColumnInfoResponse;
import io.migraflow.migration.dto.DbConnectionRequest;
import io.migraflow.migration.dto.MigrationJobApprovalHistoryResponse;
import io.migraflow.migration.dto.MigrationJobCreateRequest;
import io.migraflow.migration.dto.MigrationJobResponse;
import io.migraflow.migration.repository.MigrationJobApprovalHistoryRepository;
import io.migraflow.migration.repository.MigrationJobRepository;
import io.migraflow.project.domain.Project;
import io.migraflow.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class MigrationJobService {

    private final MigrationJobRepository migrationJobRepository;
    private final MigrationJobApprovalHistoryRepository migrationJobApprovalHistoryRepository;
    private final ProjectRepository projectRepository;
    private final MigrationSchemaService migrationSchemaService;
    private final ObjectMapper objectMapper;

    /**
     * 신규 이관 작업을 DRAFT 상태로 등록한다. 소스 타입이 {@code DB}면, 요청에 담긴 {@code {tableName}}만으로는
     * 접속할 수 없으므로 프로젝트에 등록된 AS-IS/TO-BE 접속 정보(sourceConfig/targetConfig)와 합쳐서 저장한다 —
     * 그래야 컬럼 매핑 화면(getSourceColumns/getTargetColumns)이 이 job만으로 다시 접속할 수 있다.
     *
     * @param projectId 소속 프로젝트 ID
     * @param request   작업명, 대상 엔티티, 소스 타입 등 등록 요청 정보
     * @return 생성된 이관 작업 정보
     * @throws EntityNotFoundException 프로젝트가 존재하지 않는 경우
     * @throws IllegalStateException   소스 타입이 DB인데 프로젝트에 접속 정보가 없거나 테이블을 선택하지 않은 경우
     *                                 (메시지는 {@code messages.properties}의 {@code migrationJob.register.*} 키)
     */
    public MigrationJobResponse register(Long projectId, MigrationJobCreateRequest request) {
        String sourceConfig = request.sourceConfig();
        String targetConfig = request.targetConfig();
        if (request.sourceType() == MigrationSourceType.DB) {
            Project project = getProjectOrThrow(projectId);
            sourceConfig = mergeProjectConnection(project.getSourceConfig(), request.sourceConfig());
            targetConfig = mergeProjectConnection(project.getTargetConfig(), request.targetConfig());
        }
        MigrationJob job = new MigrationJob(
                projectId,
                request.name(),
                request.targetEntityName(),
                request.sourceType(),
                sourceConfig,
                targetConfig,
                request.createdById()
        );
        migrationJobRepository.save(job);
        return toResponse(job);
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
        return toResponse(job);
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
        return toResponse(job);
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
        return toResponse(job);
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
        return toResponse(getJobOrThrow(jobId));
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
                .map(this::toResponse)
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
     * 이관 작업에 등록된 AS-IS DB 접속 정보로 접속해, 등록 시 선택한 AS-IS 테이블의 컬럼 목록을 조회한다.
     * 비밀번호를 포함한 접속 정보는 서버 안에서만 쓰이고 응답에는 포함되지 않는다.
     *
     * @param jobId 이관 작업 ID
     * @return AS-IS 테이블의 컬럼 메타데이터 목록
     * @throws EntityNotFoundException             이관 작업이 존재하지 않는 경우
     * @throws MigrationSchemaConnectionException   DB 접속에 실패한 경우
     * @throws IllegalStateException                sourceConfig가 비어있거나 저장된 형식이 아닌 경우
     *                                               (메시지는 {@code messages.properties}의 {@code migrationJob.schema.invalidConfig} 키)
     */
    @Transactional(readOnly = true)
    public List<ColumnInfoResponse> getSourceColumns(Long jobId) {
        MigrationJob job = getJobOrThrow(jobId);
        return listColumnsFromStoredConfig(job.getSourceConfig());
    }

    /**
     * 이관 작업에 등록된 TO-BE DB 접속 정보로 접속해, 등록 시 선택한 TO-BE 테이블의 컬럼 목록을 조회한다.
     * 비밀번호를 포함한 접속 정보는 서버 안에서만 쓰이고 응답에는 포함되지 않는다.
     *
     * @param jobId 이관 작업 ID
     * @return TO-BE 테이블의 컬럼 메타데이터 목록 (기본키 컬럼 여부 포함)
     * @throws EntityNotFoundException             이관 작업이 존재하지 않는 경우
     * @throws MigrationSchemaConnectionException   DB 접속에 실패한 경우
     * @throws IllegalStateException                targetConfig가 비어있거나 저장된 형식이 아닌 경우
     *                                               (메시지는 {@code messages.properties}의 {@code migrationJob.schema.invalidConfig} 키)
     */
    @Transactional(readOnly = true)
    public List<ColumnInfoResponse> getTargetColumns(Long jobId) {
        MigrationJob job = getJobOrThrow(jobId);
        return listColumnsFromStoredConfig(job.getTargetConfig());
    }

    /**
     * MigrationJob에 저장된 소스/타겟 접속 설정 JSON을 파싱해 해당 DB의 컬럼 목록을 조회한다.
     *
     * @param storedConfig {@code MigrationJob.sourceConfig} 또는 {@code targetConfig}에 저장된 JSON 문자열
     * @return 컬럼 메타데이터 목록
     * @throws IllegalStateException 설정이 비어있거나 저장된 형식이 아닌 경우
     */
    private List<ColumnInfoResponse> listColumnsFromStoredConfig(String storedConfig) {
        StoredDbConfig config = parseStoredConfig(storedConfig);
        if (config == null || config.tableName() == null || config.tableName().isBlank()) {
            throw new IllegalStateException("migrationJob.schema.invalidConfig");
        }
        DbConnectionRequest connection = new DbConnectionRequest(
                config.dbType(), config.host(), config.port(), config.database(), config.username(), config.password());
        return migrationSchemaService.listColumns(connection, config.tableName());
    }

    /**
     * {@link MigrationJob} 엔티티를, sourceConfig/targetConfig에서 추출한 AS-IS/TO-BE 테이블명과 함께 응답 DTO로 변환한다.
     *
     * @param job 변환할 이관 작업 엔티티
     * @return 변환된 이관 작업 응답 DTO
     */
    private MigrationJobResponse toResponse(MigrationJob job) {
        return new MigrationJobResponse(job, extractTableName(job.getSourceConfig()), extractTableName(job.getTargetConfig()));
    }

    /**
     * sourceConfig/targetConfig JSON에서 화면 표시용 테이블명만 뽑아낸다. 화면 표시가 목적이므로 설정이 비어있거나
     * 저장된 형식이 아니어도 예외를 던지지 않고 null을 반환한다.
     *
     * @param storedConfig {@code MigrationJob.sourceConfig} 또는 {@code targetConfig}에 저장된 JSON 문자열
     * @return 추출한 테이블명 (없거나 파싱 실패 시 null)
     */
    private String extractTableName(String storedConfig) {
        StoredDbConfig config = parseStoredConfig(storedConfig);
        return config == null ? null : config.tableName();
    }

    /**
     * sourceConfig/targetConfig JSON 문자열을 {@link StoredDbConfig}로 역직렬화한다.
     *
     * @param storedConfig {@code MigrationJob.sourceConfig} 또는 {@code targetConfig}에 저장된 JSON 문자열
     * @return 역직렬화된 접속 설정 (비어있거나 저장된 형식이 아니면 null)
     */
    private StoredDbConfig parseStoredConfig(String storedConfig) {
        if (storedConfig == null || storedConfig.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(storedConfig, StoredDbConfig.class);
        } catch (JacksonException e) {
            return null;
        }
    }

    /**
     * 프로젝트의 접속 정보(dbType/jdbcUrl/username/password)와 이관 작업 등록 요청의 {@code {tableName}}을 합쳐,
     * {@link MigrationJob}에 저장할 완전한 접속 설정 JSON을 만든다.
     *
     * @param projectConfigJson {@code Project.sourceConfig} 또는 {@code targetConfig}에 저장된 JSON 문자열
     * @param jobConfigJson     이관 작업 등록 요청의 sourceConfig/targetConfig ({@code {"tableName": "..."}} 형태)
     * @return 합쳐진 접속 설정 JSON 문자열
     * @throws IllegalStateException 프로젝트에 접속 정보가 없거나, 요청에 테이블명이 없는 경우
     */
    private String mergeProjectConnection(String projectConfigJson, String jobConfigJson) {
        if (projectConfigJson == null || projectConfigJson.isBlank()) {
            throw new IllegalStateException("migrationJob.register.missingProjectConnection");
        }
        DbConnectionRequest connection;
        try {
            connection = objectMapper.readValue(projectConfigJson, DbConnectionRequest.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("migrationJob.register.missingProjectConnection");
        }
        StoredDbConfig jobConfig = parseStoredConfig(jobConfigJson);
        if (jobConfig == null || jobConfig.tableName() == null || jobConfig.tableName().isBlank()) {
            throw new IllegalStateException("migrationJob.register.missingTableName");
        }
        StoredDbConfig merged = new StoredDbConfig(connection.dbType(), connection.host(), connection.port(),
                connection.database(), connection.username(), connection.password(), jobConfig.tableName());
        return objectMapper.writeValueAsString(merged);
    }

    /**
     * 프로젝트를 ID로 조회하고, 존재하지 않으면 예외를 던진다.
     *
     * @param projectId 프로젝트 ID
     * @return 조회된 프로젝트 엔티티
     * @throws EntityNotFoundException 프로젝트가 존재하지 않는 경우
     */
    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
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

    /**
     * MigrationJob의 sourceConfig/targetConfig JSON에 담긴 DB 접속 정보를 역직렬화하기 위한 내부 전용 구조체.
     * {@link DbConnectionRequest}에 테이블명을 더한 형태다 — 등록 시 프로젝트의 접속 정보와 요청의 테이블명을
     * 합쳐서({@link #mergeProjectConnection}) 저장한다.
     *
     * @param dbType    DB 벤더 구분
     * @param host      호스트
     * @param port      포트
     * @param database  데이터베이스명(Oracle은 SID)
     * @param username  접속 계정
     * @param password  접속 비밀번호
     * @param tableName 등록 시 선택한 테이블명
     */
    private record StoredDbConfig(String dbType, String host, String port, String database, String username,
                                   String password, String tableName) {
    }
}
