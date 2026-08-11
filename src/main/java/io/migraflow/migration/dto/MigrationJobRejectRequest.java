package io.migraflow.migration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MigrationJobRejectRequest(
        @NotNull Long actorId,
        @NotBlank String reason
) {
}
