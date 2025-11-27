/*
 * @ (#) EventLogPublisher.java    1.0    26/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 26/11/2025
 * @version: 1.0
 */

import fit.warehouse_service.events.SystemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.event-log:event_log_queue}")
    private String eventLogQueue;

    public void publishEvent(SystemEvent event) {
        try {
            // Đảm bảo luôn có timestamp
            if (event.getTimestamp() == null) {
                event.setTimestamp(LocalDateTime.now());
            }
            // Đảm bảo luôn có source service
            if (event.getSourceService() == null) {
                event.setSourceService("WAREHOUSE_SERVICE");
            }

            log.info("Publishing event to queue [{}]: [{}] - {}", eventLogQueue, event.getEventCode(), event.getAction());

            // Gửi trực tiếp vào Queue (sử dụng Default Exchange với routingKey là tên queue)
            rabbitTemplate.convertAndSend(eventLogQueue, event);

        } catch (Exception e) {
            log.error("Failed to publish event log: {}", e.getMessage(), e);
            // Không throw exception để tránh ảnh hưởng luồng nghiệp vụ chính
        }
    }
}
