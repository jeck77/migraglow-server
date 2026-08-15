package io.migraflow.migration.service;

import io.migraflow.migration.dto.ColumnInfoResponse;
import io.migraflow.migration.dto.DbConnectionRequest;
import io.migraflow.project.domain.Project;
import io.migraflow.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * 프로젝트에 등록된 AS-IS/TO-BE DB 접속 정보로 테이블·컬럼 목록을 조회하는 서비스. 프로젝트 소속 이관 작업들이
 * 접속 정보를 공유하므로, 작업 화면에 들어가는 즉시(버튼 클릭 없이) 테이블 목록을 보여줄 수 있도록 지원한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectSchemaService {

    private final ProjectRepository projectRepository;
    private final MigrationSchemaService migrationSchemaService;
    private final ObjectMapper objectMapper;

    /**
     * 프로젝트에 등록된 AS-IS DB의 테이블 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @return 테이블명 목록
     * @throws EntityNotFoundException              프로젝트가 존재하지 않는 경우
     * @throws IllegalStateException                AS-IS 접속 정보가 등록되지 않았거나 형식이 올바르지 않은 경우
     * @throws MigrationSchemaConnectionException    DB 접속에 실패한 경우
     */
    public List<String> listSourceTables(Long projectId) {
        return migrationSchemaService.listTables(resolveConnection(getProjectOrThrow(projectId).getSourceConfig()));
    }

    /**
     * 프로젝트에 등록된 TO-BE DB의 테이블 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @return 테이블명 목록
     * @throws EntityNotFoundException              프로젝트가 존재하지 않는 경우
     * @throws IllegalStateException                TO-BE 접속 정보가 등록되지 않았거나 형식이 올바르지 않은 경우
     * @throws MigrationSchemaConnectionException    DB 접속에 실패한 경우
     */
    public List<String> listTargetTables(Long projectId) {
        return migrationSchemaService.listTables(resolveConnection(getProjectOrThrow(projectId).getTargetConfig()));
    }

    /**
     * 프로젝트에 등록된 AS-IS DB의 특정 테이블 컬럼 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @param tableName 컬럼 목록을 조회할 테이블명
     * @return 컬럼 메타데이터 목록
     * @throws EntityNotFoundException              프로젝트가 존재하지 않는 경우
     * @throws IllegalStateException                AS-IS 접속 정보가 등록되지 않았거나 형식이 올바르지 않은 경우
     * @throws MigrationSchemaConnectionException    DB 접속에 실패한 경우
     */
    public List<ColumnInfoResponse> listSourceColumns(Long projectId, String tableName) {
        return migrationSchemaService.listColumns(resolveConnection(getProjectOrThrow(projectId).getSourceConfig()), tableName);
    }

    /**
     * 프로젝트에 등록된 TO-BE DB의 특정 테이블 컬럼 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @param tableName 컬럼 목록을 조회할 테이블명
     * @return 컬럼 메타데이터 목록 (기본키/외래키 정보 포함)
     * @throws EntityNotFoundException              프로젝트가 존재하지 않는 경우
     * @throws IllegalStateException                TO-BE 접속 정보가 등록되지 않았거나 형식이 올바르지 않은 경우
     * @throws MigrationSchemaConnectionException    DB 접속에 실패한 경우
     */
    public List<ColumnInfoResponse> listTargetColumns(Long projectId, String tableName) {
        return migrationSchemaService.listColumns(resolveConnection(getProjectOrThrow(projectId).getTargetConfig()), tableName);
    }

    /**
     * 프로젝트의 sourceConfig/targetConfig JSON을 {@link DbConnectionRequest}로 역직렬화한다.
     *
     * @param storedConfig {@code Project.sourceConfig} 또는 {@code targetConfig}에 저장된 JSON 문자열
     * @return 역직렬화된 접속 정보
     * @throws IllegalStateException 설정이 비어있거나 저장된 형식이 아닌 경우
     *                                (메시지는 {@code messages.properties}의 {@code project.schema.invalidConfig} 키)
     */
    private DbConnectionRequest resolveConnection(String storedConfig) {
        if (storedConfig == null || storedConfig.isBlank()) {
            throw new IllegalStateException("project.schema.invalidConfig");
        }
        try {
            return objectMapper.readValue(storedConfig, DbConnectionRequest.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("project.schema.invalidConfig");
        }
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
}
