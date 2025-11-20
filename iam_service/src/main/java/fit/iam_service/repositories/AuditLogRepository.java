/*
 * @ (#) AuditLogRepository.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import fit.iam_service.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
}
