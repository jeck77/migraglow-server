package io.migraflow.migration.dto;

import jakarta.validation.constraints.NotNull;

public record MigrationJobActionRequest(
        @NotNull Long actorId
) {
}
