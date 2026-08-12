package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationJobApprovalHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationJobApprovalHistoryRepository extends JpaRepository<MigrationJobApprovalHistory, Long> {

    /**
     * 특정 이관 작업의 제출/승인/반려 이력을 액션 시각 역순으로 조회한다.
     *
     * @param jobId 이관 작업 ID
     * @return 승인 이력 목록 (최신순)
     */
    List<MigrationJobApprovalHistory> findByJobIdOrderByActionDateDesc(Long jobId);
}
