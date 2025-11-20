/*
 * @ {#} InstrumentDeletionScheduler.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.schedulers;

import fit.warehouse_service.services.ScheduledDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*
 * @description: Scheduler to handle automatic deletion of instruments after scheduled period
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentDeletionScheduler {

    private final ScheduledDeletionService scheduledDeletionService;

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void executeScheduledDeletions() {
        log.info("Starting scheduled instrument deletion process");
        try {
            scheduledDeletionService.executePendingDeletions();
            log.info("Completed scheduled instrument deletion process");
        } catch (Exception e) {
            log.error("Error during scheduled deletion process: {}", e.getMessage(), e);
        }
    }
}
