/*
 * @ {#} ResultIngestAuditRepository.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package fit.test_order_service.repositories;

import fit.test_order_service.entities.ResultIngestAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * @description: Repository for managing ResultIngestAudit entities.
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
@Repository
public interface ResultIngestAuditRepository extends JpaRepository<ResultIngestAudit, String> {
}
