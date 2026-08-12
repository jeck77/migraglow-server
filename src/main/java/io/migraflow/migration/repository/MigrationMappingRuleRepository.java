package io.migraflow.migration.repository;

import io.migraflow.migration.domain.MigrationMappingRule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationMappingRuleRepository extends JpaRepository<MigrationMappingRule, Long> {

    /**
     * 특정 대상 엔티티에 대해 활성화된(사용 여부 true) 매핑 규칙 목록을 조회한다.
     *
     * @param targetEntityName 대상 엔티티명
     * @return 활성 매핑 규칙 목록
     */
    List<MigrationMappingRule> findByTargetEntityNameAndUseYnTrue(String targetEntityName);

    /**
     * 대상 엔티티명과 대상 필드명으로 매핑 규칙을 조회한다.
     *
     * @param targetEntityName 대상 엔티티명
     * @param targetFieldName  대상 필드명
     * @return 조건에 맞는 매핑 규칙 (없으면 빈 Optional)
     */
    Optional<MigrationMappingRule> findByTargetEntityNameAndTargetFieldName(String targetEntityName, String targetFieldName);
}
