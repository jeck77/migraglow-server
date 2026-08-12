package io.migraflow.project.controller;

import io.migraflow.project.dto.ProjectCreateRequest;
import io.migraflow.project.dto.ProjectResponse;
import io.migraflow.project.service.ProjectService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 신규 프로젝트를 등록한다.
     *
     * @param request 프로젝트명, 설명 등 등록 요청 정보
     * @return 생성된 프로젝트 정보 (201 Created)
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.register(request));
    }

    /**
     * 전체 프로젝트 목록을 조회한다.
     *
     * @return 프로젝트 목록
     */
    @GetMapping
    public List<ProjectResponse> list() {
        return projectService.list();
    }

    /**
     * 프로젝트 단건을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 상세 정보
     */
    @GetMapping("/{projectId}")
    public ProjectResponse get(@PathVariable Long projectId) {
        return projectService.get(projectId);
    }

    /**
     * 대상 프로젝트를 찾지 못해 발생한 {@link EntityNotFoundException}을 404 Not Found 응답으로 변환한다.
     *
     * @param e 발생한 예외
     * @return 예외 메시지를 담은 404 응답
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
