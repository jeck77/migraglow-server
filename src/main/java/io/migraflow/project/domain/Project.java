package io.migraflow.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "PROJECT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SOURCE_CONFIG", columnDefinition = "json")
    private String sourceConfig;

    @Column(name = "TARGET_CONFIG", columnDefinition = "json")
    private String targetConfig;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "UPDATED_DATE", insertable = false, updatable = false)
    private LocalDateTime updatedDate;

    /**
     * ACTIVE 상태의 신규 프로젝트를 생성한다.
     *
     * @param name          프로젝트명
     * @param description   프로젝트 설명
     * @param sourceConfig  AS-IS DB 접속 설정(JSON). 프로젝트 소속 이관 작업들이 공유하는 접속 정보다
     * @param targetConfig  TO-BE DB 접속 설정(JSON)
     */
    public Project(String name, String description, String sourceConfig, String targetConfig) {
        this.name = name;
        this.description = description;
        this.sourceConfig = sourceConfig;
        this.targetConfig = targetConfig;
        this.status = ProjectStatus.ACTIVE.getCode();
    }

    /**
     * 저장된 코드 값을 {@link ProjectStatus} enum으로 변환하여 반환한다.
     *
     * @return 현재 프로젝트 상태
     */
    public ProjectStatus getStatus() {
        return ProjectStatus.fromCode(status);
    }

    /**
     * 프로젝트 상태를 변경한다.
     *
     * @param status 변경할 상태
     */
    public void changeStatus(ProjectStatus status) {
        this.status = status.getCode();
    }

    /**
     * 프로젝트명과 설명을 수정한다.
     *
     * @param name        새 프로젝트명
     * @param description 새 프로젝트 설명
     */
    public void changeDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * AS-IS DB 접속 설정을 교체한다.
     *
     * @param sourceConfig 새 AS-IS DB 접속 설정(JSON)
     */
    public void changeSourceConfig(String sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    /**
     * TO-BE DB 접속 설정을 교체한다.
     *
     * @param targetConfig 새 TO-BE DB 접속 설정(JSON)
     */
    public void changeTargetConfig(String targetConfig) {
        this.targetConfig = targetConfig;
    }
}
