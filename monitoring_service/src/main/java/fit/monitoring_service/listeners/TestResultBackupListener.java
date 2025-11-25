/*
 * @ (#) TestResultBackupListener.java    1.0    24/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.listeners;/*
 * @description:
 * @author: Bao Thong
 * @date: 24/11/2025
 * @version: 1.0
 */

import fit.monitoring_service.configs.RabbitMQConfig;
import fit.monitoring_service.dtos.event.TestResultPublishedEvent;
import fit.monitoring_service.services.TestResultBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestResultBackupListener {

    private final TestResultBackupService testResultBackupService;

    @RabbitListener(queues = RabbitMQConfig.TEST_RESULT_BACKUP_QUEUE)
    public void handleTestResultEvent(TestResultPublishedEvent event) {
        log.info("Received Test Result Event for Barcode: {}", event.getBarcode());
        testResultBackupService.backupTestResult(event);
    }
}
