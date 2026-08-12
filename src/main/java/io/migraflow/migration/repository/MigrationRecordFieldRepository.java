package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationRecordField;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationRecordFieldRepository extends JpaRepository<MigrationRecordField, Long> {

    /**
     * 특정 레코드에 속한 필드 단위 값 목록을 조회한다.
     *
     * @param recordId 이관 레코드 ID
     * @return 필드 값 목록
     */
    List<MigrationRecordField> findByRecordId(Long recordId);
}
