package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationMappingRule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationMappingRuleRepository extends JpaRepository<MigrationMappingRule, Long> {

    List<MigrationMappingRule> findByTargetEntityNameAndUseYnTrue(String targetEntityName);

    Optional<MigrationMappingRule> findByTargetEntityNameAndTargetFieldName(String targetEntityName, String targetFieldName);
}
