package io.migraflow.migration.service;

import io.migraflow.migration.domain.MigrationMappingRule;
import io.migraflow.migration.domain.MigrationMappingRuleType;
import io.migraflow.migration.domain.MigrationMappingRuleValue;
import io.migraflow.migration.dto.MigrationMappingRuleCreateRequest;
import io.migraflow.migration.dto.MigrationMappingRuleResponse;
import io.migraflow.migration.repository.MigrationMappingRuleRepository;
import io.migraflow.migration.repository.MigrationMappingRuleValueRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AS-IS 컬럼과 TO-BE 컬럼을 잇는 매핑 규칙(및 {@code VALUE_MAP} 값 쌍)을 등록/조회/비활성화하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MigrationMappingRuleService {

    private final MigrationMappingRuleRepository migrationMappingRuleRepository;
    private final MigrationMappingRuleValueRepository migrationMappingRuleValueRepository;

    /**
     * 매핑 규칙을 등록한다. 같은 대상 엔티티+대상 필드 조합의 규칙이 이미 있으면 기존 규칙(및 값 매핑)을 지우고 새로 만든다.
     *
     * @param request 등록할 매핑 규칙 정보
     * @return 생성된 매핑 규칙 정보
     * @throws IllegalArgumentException ruleType별 필수값이 빠진 경우 (메시지는 {@code messages.properties}의
     *                                   {@code migrationMappingRule.create.*} 키)
     */
    public MigrationMappingRuleResponse create(MigrationMappingRuleCreateRequest request) {
        validate(request);

        migrationMappingRuleRepository
                .findByTargetEntityNameAndTargetFieldName(request.targetEntityName(), request.targetFieldName())
                .ifPresent(existing -> {
                    migrationMappingRuleValueRepository.deleteAll(migrationMappingRuleValueRepository.findByRuleId(existing.getId()));
                    migrationMappingRuleRepository.delete(existing);
                });
        // 같은 (TARGET_ENTITY_NAME, TARGET_FIELD_NAME) 유니크 제약을 가진 새 규칙을 곧바로 저장하므로,
        // Hibernate가 flush 시 INSERT를 DELETE보다 먼저 실행해 제약 위반이 나지 않도록 삭제를 즉시 flush한다.
        migrationMappingRuleRepository.flush();

        String sourceFieldName = request.ruleType() == MigrationMappingRuleType.FIXED_VALUE ? null : request.sourceFieldName();
        MigrationMappingRule rule = new MigrationMappingRule(
                request.targetEntityName(), sourceFieldName, request.targetFieldName(), request.ruleType(), request.expression());
        migrationMappingRuleRepository.save(rule);

        List<MigrationMappingRuleValue> values = List.of();
        if (request.ruleType() == MigrationMappingRuleType.VALUE_MAP) {
            values = request.valueMap().stream()
                    .map(pair -> migrationMappingRuleValueRepository.save(
                            new MigrationMappingRuleValue(rule.getId(), pair.sourceValue(), pair.targetValue())))
                    .toList();
        }
        return new MigrationMappingRuleResponse(rule, values);
    }

    /**
     * 특정 대상 엔티티에 활성화된 매핑 규칙 목록을 조회한다.
     *
     * @param targetEntityName 대상 엔티티명
     * @return 활성 매핑 규칙 목록
     */
    @Transactional(readOnly = true)
    public List<MigrationMappingRuleResponse> listByTargetEntity(String targetEntityName) {
        return migrationMappingRuleRepository.findByTargetEntityNameAndUseYnTrue(targetEntityName).stream()
                .map(rule -> new MigrationMappingRuleResponse(rule, migrationMappingRuleValueRepository.findByRuleId(rule.getId())))
                .toList();
    }

    /**
     * 매핑 규칙을 비활성화하여 더 이상 적용되지 않도록 한다.
     *
     * @param ruleId 비활성화할 매핑 규칙 ID
     * @throws EntityNotFoundException 매핑 규칙이 존재하지 않는 경우
     */
    public void deactivate(Long ruleId) {
        MigrationMappingRule rule = migrationMappingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new EntityNotFoundException("MigrationMappingRule not found: " + ruleId));
        rule.deactivate();
    }

    /**
     * ruleType별로 요구되는 필수값이 채워졌는지 검증한다.
     *
     * @param request 검증할 매핑 규칙 등록 요청
     * @throws IllegalArgumentException 필수값이 빠진 경우
     */
    private void validate(MigrationMappingRuleCreateRequest request) {
        MigrationMappingRuleType ruleType = request.ruleType();
        if (ruleType == MigrationMappingRuleType.FIXED_VALUE) {
            if (request.expression() == null || request.expression().isBlank()) {
                throw new IllegalArgumentException("migrationMappingRule.create.expressionRequired");
            }
            return;
        }
        if (request.sourceFieldName() == null || request.sourceFieldName().isBlank()) {
            throw new IllegalArgumentException("migrationMappingRule.create.sourceFieldRequired");
        }
        if (ruleType == MigrationMappingRuleType.EXPRESSION
                && (request.expression() == null || request.expression().isBlank())) {
            throw new IllegalArgumentException("migrationMappingRule.create.expressionRequired");
        }
        if (ruleType == MigrationMappingRuleType.VALUE_MAP
                && (request.valueMap() == null || request.valueMap().isEmpty())) {
            throw new IllegalArgumentException("migrationMappingRule.create.valueMapRequired");
        }
    }
}
