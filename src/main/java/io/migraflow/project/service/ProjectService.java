package io.migraflow.project.service;

import io.migraflow.project.domain.Project;
import io.migraflow.project.dto.ProjectCreateRequest;
import io.migraflow.project.dto.ProjectResponse;
import io.migraflow.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    /**
     * 신규 프로젝트를 ACTIVE 상태로 등록한다.
     *
     * @param request 프로젝트명, 설명 등 등록 요청 정보
     * @return 생성된 프로젝트 정보
     */
    public ProjectResponse register(ProjectCreateRequest request) {
        Project project = new Project(request.name(), request.description());
        projectRepository.save(project);
        return new ProjectResponse(project);
    }

    /**
     * 전체 프로젝트 목록을 조회한다.
     *
     * @return 프로젝트 목록
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> list() {
        return projectRepository.findAll().stream()
                .map(ProjectResponse::new)
                .toList();
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
        return new ProjectResponse(getProjectOrThrow(projectId));
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
