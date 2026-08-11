package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationJob;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationJobRepository extends JpaRepository<MigrationJob, Long> {

    List<MigrationJob> findByProjectId(Long projectId);
}
