/*
 * @ {#} NotificationServiceImpl.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services.impl;

import fit.instrument_service.entities.BloodSample;
import fit.instrument_service.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
 * @description: Service xử lý thông báo trong hệ thống
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    @Override
    public void notifySampleStatusUpdate(BloodSample sample) {
        log.info("Notification: Sample {} status updated to {}", sample.getBarcode(), sample.getStatus());
        // TODO: Implement actual notification via RabbitMQ/WebSocket
        // Could send to a notification queue for real-time updates to UI
    }

    @Override
    public void notifyWorkflowCompletion(String workflowId, String instrumentId) {
        log.info("Notification: Workflow {} completed for instrument {}",
                workflowId, instrumentId);
        // TODO: Implement actual notification via RabbitMQ/WebSocket
    }

    @Override
    public void notifyInsufficientReagents(String instrumentId) {
        log.warn("Notification: Insufficient reagents for instrument {}", instrumentId);
        // TODO: Implement actual notification via RabbitMQ/WebSocket
    }
}
