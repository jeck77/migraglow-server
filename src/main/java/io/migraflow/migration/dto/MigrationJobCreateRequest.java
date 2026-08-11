package io.migraflow.migration.dto;

import io.migraflow.migration.domain.MigrationSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MigrationJobCreateRequest(
        @NotBlank String name,
        @NotBlank String targetEntityName,
        @NotNull MigrationSourceType sourceType,
        String sourceConfig,
        @NotNull Long createdById
) {
}
