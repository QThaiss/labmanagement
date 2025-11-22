/*
 * @ {#} FlaggingServiceImpl.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.test_order_service.entities.FlaggingApplied;
import fit.test_order_service.entities.FlaggingConfigRule;
import fit.test_order_service.entities.FlaggingConfigVersion;
import fit.test_order_service.entities.TestResult;
import fit.test_order_service.repositories.FlaggingAppliedRepository;
import fit.test_order_service.repositories.FlaggingConfigRuleRepository;
import fit.test_order_service.repositories.FlaggingConfigVersionRepository;
import fit.test_order_service.services.FlaggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * @description: Implementation of FlaggingService to apply flagging rules to test results.
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FlaggingServiceImpl implements FlaggingService {
    private final FlaggingAppliedRepository flaggingAppliedRepository;

    private final FlaggingConfigVersionRepository configVersionRepository;

    private final FlaggingConfigRuleRepository ruleRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public void applyFlaggingRules(TestResult result) {
        try {
            // Lấy version config hiện hành (nếu null → log warn, bỏ qua).
            FlaggingConfigVersion latestConfig = getLatestFlaggingConfig();
            if (latestConfig == null) {
                log.warn("No flagging configuration found, skipping flagging for result: {}", result.getResultId());
                return;
            }

            // Áp dụng các quy tắc đánh dấu dựa trên cấu hình
            List<FlaggingConfigRule> rules = ruleRepository.findByConfigVersionId(latestConfig.getId());
            if (rules.isEmpty()) {
                log.warn("No rules found for config version {}", latestConfig.getId());
                return;
            }

            // Kiểm tra và áp dụng từng quy tắc
            for (FlaggingConfigRule  rule : rules) {
                if (matchesRule(rule, result)) {
                    createFlaggingApplied(result, rule, latestConfig.getVersion());
                }
            }

        } catch (Exception e) {
            log.error("Error applying flagging rules to result {}: {}", result.getResultId(), e.getMessage());
        }
    }

    // Lấy cấu hình đánh dấu mới nhất
    private FlaggingConfigVersion getLatestFlaggingConfig() {
        return configVersionRepository.findTopByOrderByActivatedAtDesc().orElse(null);
    }

    // Kiểm tra xem kết quả có thỏa mãn quy tắc hay không
    private boolean matchesRule(FlaggingConfigRule rule, TestResult result) {
        try {
            JsonNode node = objectMapper.readTree(rule.getConditionJson());

            // Nếu rule có abnormalFlag
            if (node.has("abnormalFlag")) {
                String abnormal = node.get("abnormalFlag").asText();
                if (result.getAbnormalFlag() != null &&
                        result.getAbnormalFlag().name().equalsIgnoreCase(abnormal)) {
                    return true;
                }
            }

            // Nếu rule có analytePattern
            if (node.has("analytePattern")) {
                String pattern = node.get("analytePattern").asText();
                if (result.getAnalyteName() != null &&
                        result.getAnalyteName().toLowerCase().contains(pattern.toLowerCase())) {
                    return true;
                }
            }

            // Nếu rule có valueThreshold
            if (node.has("valueThreshold")) {
                double threshold = node.get("valueThreshold").asDouble();
                try {
                    double value = Double.parseDouble(result.getValueText());
                    if (value >= threshold) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }

        } catch (Exception e) {
            log.warn("Failed to evaluate rule {}: {}", rule.getFlagCode(), e.getMessage());
        }
        return false;
    }

    // Tạo bản ghi FlaggingApplied khi một quy tắc được áp dụng
    private void createFlaggingApplied(TestResult result, FlaggingConfigRule rule, Integer ruleVersion) {
        FlaggingApplied flagging = FlaggingApplied.builder()
                .resultId(result.getResultId())
                .ruleId(rule.getRuleId())
                .ruleVersion(ruleVersion)
                .flagCode(rule.getFlagCode())
                .severity(rule.getSeverity())
                .contextJson("{\"analyte\":\"" + result.getAnalyteName() + "\",\"value\":\"" + result.getValueText() + "\"}")
                .build();

        flaggingAppliedRepository.save(flagging);
    }
}
