/*
 * @ (#) TestResultSyncRequestListener.java    1.0    25/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.listeners;/*
 * @description:
 * @author: Bao Thong
 * @date: 25/11/2025
 * @version: 1.0
 */

import fit.instrument_service.configs.RabbitMQConfig;
import fit.instrument_service.events.TestResultSyncRequestEvent;
import fit.instrument_service.services.impl.TestResultSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestResultSyncRequestListener {

    private final TestResultSyncService testResultSyncService;

    @RabbitListener(queues = RabbitMQConfig.SYNC_REQUEST_QUEUE)
    public void handleSyncRequest(TestResultSyncRequestEvent event) {
        log.info("Received Sync Request from: {}", event.getRequestedBy());
        testResultSyncService.processSyncRequest(event);
    }
}
