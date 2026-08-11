package io.migraflow.project.dto;

import io.migraflow.project.domain.Project;
import io.migraflow.project.domain.ProjectStatus;
import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        ProjectStatus status,
        LocalDateTime createDate,
        LocalDateTime updatedDate
) {

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
