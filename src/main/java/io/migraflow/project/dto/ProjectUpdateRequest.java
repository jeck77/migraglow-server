package io.migraflow.project.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 프로젝트 수정 요청 정보. {@code sourceConfig}/{@code targetConfig}는 비어있으면 기존 값을 그대로 유지하고,
 * 값이 있으면 통째로 교체한다 — 응답에서 비밀번호를 내려주지 않으므로 부분 수정(예: 비밀번호만 변경)은 지원하지 않는다.
 *
 * @param name          프로젝트명
 * @param description   프로젝트 설명
 * @param sourceConfig  AS-IS DB 접속 설정(JSON). 비어있으면 기존 값 유지
 * @param targetConfig  TO-BE DB 접속 설정(JSON). 비어있으면 기존 값 유지
 */
public record ProjectUpdateRequest(
        @NotBlank String name,
        String description,
        String sourceConfig,
        String targetConfig
) {
}
