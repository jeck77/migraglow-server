package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationExecutionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationExecutionHistoryRepository extends JpaRepository<MigrationExecutionHistory, Long> {

    List<MigrationExecutionHistory> findByJobIdOrderByStartDateDesc(Long jobId);
}
