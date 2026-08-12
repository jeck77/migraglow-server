package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationMappingRuleValue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationMappingRuleValueRepository extends JpaRepository<MigrationMappingRuleValue, Long> {

    /**
     * 특정 매핑 규칙(VALUE_MAP)에 등록된 AS-IS/TO-BE 값 쌍 목록을 조회한다.
     *
     * @param ruleId 매핑 규칙 ID
     * @return 값 매핑 목록
     */
    List<MigrationMappingRuleValue> findByRuleId(Long ruleId);
}
