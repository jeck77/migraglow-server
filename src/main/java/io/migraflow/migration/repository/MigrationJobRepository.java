package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationJob;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationJobRepository extends JpaRepository<MigrationJob, Long> {

    /**
     * 특정 프로젝트에 속한 이관 작업 목록을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @return 해당 프로젝트의 이관 작업 목록
     */
    List<MigrationJob> findByProjectId(Long projectId);
}
