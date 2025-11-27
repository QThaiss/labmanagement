/*
 * @ (#) EventLogRepositoryCustom.java    1.0    26/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 26/11/2025
 * @version: 1.0
 */

import fit.monitoring_service.dtos.request.EventLogFilterRequest;
import fit.monitoring_service.entities.EventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventLogRepositoryCustom {
    Page<EventLog> searchEventLogs(EventLogFilterRequest filter, Pageable pageable);
}
