package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationJobApprovalHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationJobApprovalHistoryRepository extends JpaRepository<MigrationJobApprovalHistory, Long> {

    List<MigrationJobApprovalHistory> findByJobIdOrderByActionDateDesc(Long jobId);
}
