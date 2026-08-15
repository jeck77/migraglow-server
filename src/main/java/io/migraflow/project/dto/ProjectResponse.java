package io.migraflow.project.dto;

import io.migraflow.project.domain.Project;
import io.migraflow.project.domain.ProjectStatus;
import java.time.LocalDateTime;

/**
 * 프로젝트 조회/응답용 DTO. AS-IS/TO-BE 접속 정보 중 비밀번호를 제외한 나머지(dbType/host/port/database/username)는
 * 그대로 내려준다 — 이 값들은 화면에 다시 보여줘도 안전하고, 그래야 수정 화면에서 매번 다시 입력하지 않아도 된다.
 * 비밀번호는 절대 내려주지 않는다.
 *
 * @param id               프로젝트 ID
 * @param name             프로젝트명
 * @param description      프로젝트 설명
 * @param sourceConfigured AS-IS DB 접속 설정이 등록되어 있는지 여부
 * @param sourceDbType     AS-IS DB 벤더 구분 (미등록 시 null)
 * @param sourceHost       AS-IS 호스트 (미등록 시 null)
 * @param sourcePort       AS-IS 포트 (미등록 시 null)
 * @param sourceDatabase   AS-IS 데이터베이스명 (미등록 시 null)
 * @param sourceUsername   AS-IS 접속 계정 (미등록 시 null)
 * @param targetConfigured TO-BE DB 접속 설정이 등록되어 있는지 여부
 * @param targetDbType     TO-BE DB 벤더 구분 (미등록 시 null)
 * @param targetHost       TO-BE 호스트 (미등록 시 null)
 * @param targetPort       TO-BE 포트 (미등록 시 null)
 * @param targetDatabase   TO-BE 데이터베이스명 (미등록 시 null)
 * @param targetUsername   TO-BE 접속 계정 (미등록 시 null)
 * @param status           현재 프로젝트 상태
 * @param createDate       생성 일시
 * @param updatedDate      수정 일시
 */
public record ProjectResponse(
        Long id,
        String name,
        String description,
        boolean sourceConfigured,
        String sourceDbType,
        String sourceHost,
        String sourcePort,
        String sourceDatabase,
        String sourceUsername,
        boolean targetConfigured,
        String targetDbType,
        String targetHost,
        String targetPort,
        String targetDatabase,
        String targetUsername,
        ProjectStatus status,
        LocalDateTime createDate,
        LocalDateTime updatedDate
) {

    /**
     * {@link Project} 엔티티와, 그 sourceConfig/targetConfig에서 비밀번호를 뺀 접속 정보 뷰를 응답 DTO로 변환한다.
     *
     * @param project          변환할 프로젝트 엔티티
     * @param sourceConnection AS-IS 접속 정보 중 비밀번호를 뺀 뷰 (미등록/파싱 실패 시 null)
     * @param targetConnection TO-BE 접속 정보 중 비밀번호를 뺀 뷰 (미등록/파싱 실패 시 null)
     */
    public ProjectResponse(Project project, ProjectConnectionView sourceConnection, ProjectConnectionView targetConnection) {
        this(
                project.getId(),
                project.getName(),
                project.getDescription(),
                sourceConnection != null,
                sourceConnection == null ? null : sourceConnection.dbType(),
                sourceConnection == null ? null : sourceConnection.host(),
                sourceConnection == null ? null : sourceConnection.port(),
                sourceConnection == null ? null : sourceConnection.database(),
                sourceConnection == null ? null : sourceConnection.username(),
                targetConnection != null,
                targetConnection == null ? null : targetConnection.dbType(),
                targetConnection == null ? null : targetConnection.host(),
                targetConnection == null ? null : targetConnection.port(),
                targetConnection == null ? null : targetConnection.database(),
                targetConnection == null ? null : targetConnection.username(),
                project.getStatus(),
                project.getCreateDate(),
                project.getUpdatedDate()
        );
    }
}
