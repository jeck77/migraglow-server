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

    public ProjectResponse register(ProjectCreateRequest request) {
        Project project = new Project(request.name(), request.description());
        projectRepository.save(project);
        return new ProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list() {
        return projectRepository.findAll().stream()
                .map(ProjectResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long projectId) {
        return new ProjectResponse(getProjectOrThrow(projectId));
    }

    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
    }
}
