package io.migraflow.project.dto;

import io.migraflow.project.domain.Project;
import io.migraflow.project.domain.ProjectStatus;
import java.time.LocalDateTime;

/**
 * 프로젝트 조회/응답용 DTO.
 *
 * @param id          프로젝트 ID
 * @param name        프로젝트명
 * @param description 프로젝트 설명
 * @param status      현재 프로젝트 상태
 * @param createDate  생성 일시
 * @param updatedDate 수정 일시
 */
public record ProjectResponse(
        Long id,
        String name,
        String description,
        ProjectStatus status,
        LocalDateTime createDate,
        LocalDateTime updatedDate
) {

    /**
     * {@link Project} 엔티티를 응답 DTO로 변환한다.
     *
     * @param project 변환할 프로젝트 엔티티
     */
    public ProjectResponse(Project project) {
        this(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreateDate(),
                project.getUpdatedDate()
        );
    }
}
