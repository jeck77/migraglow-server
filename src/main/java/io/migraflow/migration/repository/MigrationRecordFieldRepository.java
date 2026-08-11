package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationRecordField;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationRecordFieldRepository extends JpaRepository<MigrationRecordField, Long> {

    List<MigrationRecordField> findByRecordId(Long recordId);
}
