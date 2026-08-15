package io.migraflow.project.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 신규 프로젝트 등록 요청 정보.
 *
 * @param name          프로젝트명
 * @param description   프로젝트 설명
 * @param sourceConfig  AS-IS DB 접속 설정(JSON, 선택)
 * @param targetConfig  TO-BE DB 접속 설정(JSON, 선택)
 */
public record ProjectCreateRequest(
        @NotBlank String name,
        String description,
        String sourceConfig,
        String targetConfig
) {
}
