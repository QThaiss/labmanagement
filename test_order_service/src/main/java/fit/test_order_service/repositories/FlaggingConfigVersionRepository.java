/*
 * @ {#} FlaggingConfigVersionRepository.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.repositories;

import fit.test_order_service.entities.FlaggingConfigVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * @description: Repository for managing FlaggingApplied entities
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
@Repository
public interface FlaggingConfigVersionRepository extends JpaRepository<FlaggingConfigVersion, String> {
    /**
     * Tìm phiên bản cấu hình đánh dấu mới nhất dựa trên thời gian kích hoạt.
     *
     * @return Optional chứa FlaggingConfigVersion nếu tìm thấy.
     */
    Optional<FlaggingConfigVersion> findTopByOrderByActivatedAtDesc();
}
