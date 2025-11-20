/*
 * @ {#} FlaggingConfigRuleRepository.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.repositories;

import fit.test_order_service.entities.FlaggingConfigRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * @description: Repository interface for FlaggingConfigRule entity.
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@Repository
public interface FlaggingConfigRuleRepository extends JpaRepository<FlaggingConfigRule, String> {
    /**
     * Tìm kiếm các FlaggingConfigRule theo configVersionId.
     *
     * @param configVersionId Mã phiên bản cấu hình
     * @return Danh sách các FlaggingConfigRule liên quan đến phiên bản cấu hình
     */
    List<FlaggingConfigRule> findByConfigVersionId(String configVersionId);
}
