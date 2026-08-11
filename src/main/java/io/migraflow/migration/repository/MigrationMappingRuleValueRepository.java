package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationMappingRuleValue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationMappingRuleValueRepository extends JpaRepository<MigrationMappingRuleValue, Long> {

    List<MigrationMappingRuleValue> findByRuleId(Long ruleId);
}
