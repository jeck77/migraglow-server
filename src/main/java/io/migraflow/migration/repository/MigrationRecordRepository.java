package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationRecordRepository extends JpaRepository<MigrationRecord, Long> {

    List<MigrationRecord> findByJobId(Long jobId);

    List<MigrationRecord> findByJobIdAndStatus(Long jobId, Integer status);
}
