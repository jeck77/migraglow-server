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

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "UPDATED_DATE", insertable = false, updatable = false)
    private LocalDateTime updatedDate;

    /**
     * ACTIVE 상태의 신규 프로젝트를 생성한다.
     *
     * @param name        프로젝트명
     * @param description 프로젝트 설명
     */
    public Project(String name, String description) {
        this.name = name;
        this.description = description;
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
}
