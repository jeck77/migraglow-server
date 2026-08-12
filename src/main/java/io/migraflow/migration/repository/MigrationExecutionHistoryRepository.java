package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationExecutionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationExecutionHistoryRepository extends JpaRepository<MigrationExecutionHistory, Long> {

    /**
     * 특정 이관 작업의 배치 실행 이력을 시작 시각 역순으로 조회한다.
     *
     * @param jobId 이관 작업 ID
     * @return 실행 이력 목록 (최신순)
     */
    List<MigrationExecutionHistory> findByJobIdOrderByStartDateDesc(Long jobId);
}
