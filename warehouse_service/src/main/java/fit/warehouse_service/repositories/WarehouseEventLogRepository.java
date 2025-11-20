/*
 * @ (#) WarehouseEventLogRepository.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.WarehouseEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */
@Repository
public interface WarehouseEventLogRepository extends JpaRepository<WarehouseEventLog, String> {
}
