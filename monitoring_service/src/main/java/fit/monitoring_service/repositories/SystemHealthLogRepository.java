/*
 * @ (#) SystemHealthLogRepository.java    1.0    19/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 19/11/2025
 * @version: 1.0
 */

import fit.monitoring_service.entities.SystemHealthLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemHealthLogRepository extends MongoRepository<SystemHealthLog, String> {
}
