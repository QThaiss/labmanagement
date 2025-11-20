/*
 * @ {#} AccessLogRepository.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.repositories;

import fit.patient_service.entities.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * @description: Repository for AccessLog entity to handle database operations.
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, String> {
}
