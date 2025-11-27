/*
 * @ (#) EventLogListener.java    1.0    25/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.listeners;/*
 * @description:
 * @author: Bao Thong
 * @date: 25/11/2025
 * @version: 1.0
 */

import fit.monitoring_service.dtos.event.SystemEvent;
import fit.monitoring_service.services.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventLogListener {

    private final EventLogService eventLogService;

    /**
     * Lắng nghe sự kiện từ Queue chung cho logging.
     * Tên queue "event.log.queue" cần được khai báo trong RabbitMQConfig hoặc application.properties
     */
    @RabbitListener(queues = "${rabbitmq.queue.event-log:event_log_queue}")
    public void handleEventLog(SystemEvent event) {
        log.info("Received event message from service: {}", event.getSourceService());
        eventLogService.saveEventLog(event);
    }
}