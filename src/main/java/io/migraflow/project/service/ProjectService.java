package io.migraflow.project.service;

import io.migraflow.project.domain.Project;
import io.migraflow.project.dto.ProjectConnectionView;
import io.migraflow.project.dto.ProjectCreateRequest;
import io.migraflow.project.dto.ProjectResponse;
import io.migraflow.project.dto.ProjectUpdateRequest;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;

    /**
     * 신규 프로젝트를 ACTIVE 상태로 등록한다.
     *
     * @param request 프로젝트명, 설명 등 등록 요청 정보
     * @return 생성된 프로젝트 정보
     */
    public ProjectResponse register(ProjectCreateRequest request) {
        Project project = new Project(request.name(), request.description(), request.sourceConfig(), request.targetConfig());
        projectRepository.save(project);
        return toResponse(project);
    }

    /**
     * 전체 프로젝트 목록을 조회한다.
     *
     * @return 프로젝트 목록
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> list() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 프로젝트명/설명을 수정하고, 접속 설정이 요청에 담겨 있으면 교체한다. sourceConfig/targetConfig의 비밀번호가
     * 비어있으면 기존 비밀번호를 그대로 유지한다 — 응답에서 비밀번호를 내려주지 않으므로 화면에서 다시 채워 보낼 수
     * 없기 때문이다. dbType/host/port/database/username은 화면에 이미 채워져 있으므로 그대로 요청값으로 교체된다.
     *
     * @param projectId 프로젝트 ID
     * @param request   수정할 프로젝트명/설명, 필요 시 새 접속 설정
     * @return 수정된 프로젝트 정보
     * @throws EntityNotFoundException 프로젝트가 존재하지 않는 경우
     */
    public ProjectResponse update(Long projectId, ProjectUpdateRequest request) {
        Project project = getProjectOrThrow(projectId);
        project.changeDetails(request.name(), request.description());
        if (request.sourceConfig() != null && !request.sourceConfig().isBlank()) {
            project.changeSourceConfig(mergeWithExistingPassword(project.getSourceConfig(), request.sourceConfig()));
        }
        if (request.targetConfig() != null && !request.targetConfig().isBlank()) {
            project.changeTargetConfig(mergeWithExistingPassword(project.getTargetConfig(), request.targetConfig()));
        }
        return toResponse(project);
    }

    /**
     * 프로젝트 단건을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 상세 정보
     * @throws EntityNotFoundException 프로젝트가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public ProjectResponse get(Long projectId) {
        return toResponse(getProjectOrThrow(projectId));
    }

    /**
     * 새로 들어온 접속 설정 JSON의 비밀번호가 비어있으면, 기존에 저장돼 있던 비밀번호를 그대로 채워 넣는다.
     * 비밀번호 외의 값(dbType/host/port/database/username)은 항상 새로 들어온 값을 쓴다.
     *
     * @param existingConfigJson 수정 전 {@code Project.sourceConfig} 또는 {@code targetConfig}
     * @param newConfigJson      수정 요청으로 들어온 접속 설정 JSON (비밀번호가 비어있을 수 있다)
     * @return 저장할 접속 설정 JSON (비밀번호가 채워진 상태)
     */
    private String mergeWithExistingPassword(String existingConfigJson, String newConfigJson) {
        StoredConnection incoming = parseStoredConnection(newConfigJson);
        if (incoming == null) {
            return newConfigJson;
        }
        if (incoming.password() != null && !incoming.password().isBlank()) {
            return newConfigJson;
        }
        StoredConnection existing = parseStoredConnection(existingConfigJson);
        String existingPassword = existing == null ? null : existing.password();
        StoredConnection merged = new StoredConnection(
                incoming.dbType(), incoming.host(), incoming.port(), incoming.database(), incoming.username(), existingPassword);
        return objectMapper.writeValueAsString(merged);
    }

    /**
     * {@link Project} 엔티티를, sourceConfig/targetConfig에서 비밀번호를 뺀 접속 정보 뷰와 함께 응답 DTO로 변환한다.
     *
     * @param project 변환할 프로젝트 엔티티
     * @return 변환된 프로젝트 응답 DTO
     */
    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(project, toView(project.getSourceConfig()), toView(project.getTargetConfig()));
    }

    /**
     * sourceConfig/targetConfig JSON을 파싱해 비밀번호를 뺀 화면 표시용 뷰로 변환한다. 비어있거나 저장된 형식이
     * 아니어도 예외를 던지지 않고 null을 반환한다.
     *
     * @param storedConfig {@code Project.sourceConfig} 또는 {@code targetConfig}에 저장된 JSON 문자열
     * @return 비밀번호를 뺀 접속 정보 뷰 (없거나 파싱 실패 시 null)
     */
    private ProjectConnectionView toView(String storedConfig) {
        StoredConnection connection = parseStoredConnection(storedConfig);
        return connection == null ? null
                : new ProjectConnectionView(connection.dbType(), connection.host(), connection.port(), connection.database(), connection.username());
    }

    /**
     * sourceConfig/targetConfig JSON 문자열을 {@link StoredConnection}으로 역직렬화한다.
     *
     * @param storedConfig {@code Project.sourceConfig} 또는 {@code targetConfig}에 저장된 JSON 문자열
     * @return 역직렬화된 접속 정보 (비어있거나 저장된 형식이 아니면 null)
     */
    private StoredConnection parseStoredConnection(String storedConfig) {
        if (storedConfig == null || storedConfig.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(storedConfig, StoredConnection.class);
        } catch (JacksonException e) {
            return null;
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

    /**
     * Project의 sourceConfig/targetConfig JSON에 담긴 DB 접속 정보를 역직렬화하기 위한 내부 전용 구조체.
     * 비밀번호까지 포함한 전체 형태를 담는다 — {@link #mergeWithExistingPassword}가 기존 비밀번호를 읽어와야 하므로.
     *
     * @param dbType   DB 벤더 구분
     * @param host     호스트
     * @param port     포트
     * @param database 데이터베이스명(Oracle은 SID)
     * @param username 접속 계정
     * @param password 접속 비밀번호
     */
    private record StoredConnection(String dbType, String host, String port, String database, String username, String password) {
    }
}
