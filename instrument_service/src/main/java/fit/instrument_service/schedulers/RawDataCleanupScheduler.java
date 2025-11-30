/*
 * @ (#) RawDataCleanupScheduler.java    1.0    29/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.schedulers;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/11/2025
 * @version: 1.0
 */

import fit.instrument_service.services.RawTestResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RawDataCleanupScheduler {

    private final RawTestResultService rawTestResultService;

    /**
     * SRS 3.6.1.6: Auto Delete Raw Test Results
     * Background job verify and auto delete old raw test results.
     * Chạy định kỳ vào 02:00 sáng mỗi ngày.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    // Hoặc dùng fixedRate để test: @Scheduled(fixedRate = 60000)
    public void runCleanupJob() {
        log.info("Triggering scheduled raw data cleanup...");
        try {
            rawTestResultService.executeAutoDeletion();
        } catch (Exception e) {
            log.error("Error during scheduled raw data cleanup", e);
        }
    }
}