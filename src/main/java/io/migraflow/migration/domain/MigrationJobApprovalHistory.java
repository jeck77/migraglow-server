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

    /**
     * 이관 작업에 대한 제출/승인/반려 이력 한 건을 생성한다.
     *
     * @param jobId      대상 이관 작업 ID
     * @param actionType 수행된 액션 타입 (제출/승인/반려)
     * @param actorId    액션을 수행한 사용자 ID
     * @param reason     사유 (반려 시)
     */
    public MigrationJobApprovalHistory(Long jobId, MigrationJobActionType actionType, Long actorId, String reason) {
        this.jobId = jobId;
        this.actionType = actionType.getCode();
        this.actorId = actorId;
        this.actionDate = LocalDateTime.now();
        this.reason = reason;
    }

    /**
     * 저장된 코드 값을 {@link MigrationJobActionType} enum으로 변환하여 반환한다.
     *
     * @return 수행된 액션 타입
     */
    public MigrationJobActionType getActionType() {
        return MigrationJobActionType.fromCode(actionType);
    }
}
