package io.migraflow.migration.domain;

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
@Table(name = "MIGRATION_JOB_APPROVAL_HISTORY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MigrationJobApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Column(name = "ACTION_TYPE", nullable = false)
    private Integer actionType;

    @Column(name = "ACTOR_ID", nullable = false)
    private Long actorId;

    @Column(name = "ACTION_DATE", nullable = false)
    private LocalDateTime actionDate;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    private LocalDateTime createDate;

    public MigrationJobApprovalHistory(Long jobId, MigrationJobActionType actionType, Long actorId, String reason) {
        this.jobId = jobId;
        this.actionType = actionType.getCode();
        this.actorId = actorId;
        this.actionDate = LocalDateTime.now();
        this.reason = reason;
    }

    public MigrationJobActionType getActionType() {
        return MigrationJobActionType.fromCode(actionType);
    }
}
