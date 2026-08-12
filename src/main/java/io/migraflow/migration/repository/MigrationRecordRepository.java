package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationRecordRepository extends JpaRepository<MigrationRecord, Long> {

    /**
     * 특정 이관 작업에 속한 레코드 목록을 조회한다.
     *
     * @param jobId 이관 작업 ID
     * @return 레코드 목록
     */
    List<MigrationRecord> findByJobId(Long jobId);

    /**
     * 특정 이관 작업에 속한 레코드 중 지정한 상태의 레코드 목록을 조회한다.
     *
     * @param jobId  이관 작업 ID
     * @param status 조회할 레코드 상태 코드
     * @return 조건에 맞는 레코드 목록
     */
    List<MigrationRecord> findByJobIdAndStatus(Long jobId, Integer status);
}
