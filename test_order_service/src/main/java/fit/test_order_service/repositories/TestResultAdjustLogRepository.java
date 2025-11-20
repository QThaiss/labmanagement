/*
 * @ (#) TestResultAdjustLogRepository.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import fit.test_order_service.entities.TestResultAdjustLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestResultAdjustLogRepository extends JpaRepository<TestResultAdjustLog, String> {
}
