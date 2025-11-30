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

    /**
     * SRS 3.6.1.4: Test Results Sync-up
     * Lắng nghe yêu cầu đồng bộ từ Test Order Service hoặc Monitoring Service.
     * Queue này cần được bind với routing key "instrument.sync.request" trong RabbitMQConfig.
     *
     * @param request Sự kiện chứa danh sách barcodes cần đồng bộ.
     */
    @RabbitListener(queues = RabbitMQConfig.SYNC_REQUEST_QUEUE)
    public void handleSyncRequest(TestResultSyncRequestEvent request) {
        log.info("Received Sync Request. ID: {}, Requested By: {}",
                request.getRequestId(), request.getRequestedBy());

        try {
            // Gọi service để tìm và publish lại kết quả
            testResultSyncService.processSyncRequest(request);

            log.info("Completed processing sync request: {}", request.getRequestId());
        } catch (Exception e) {
            log.error("Error processing sync request: {}", request.getRequestId(), e);
        }
    }
}
