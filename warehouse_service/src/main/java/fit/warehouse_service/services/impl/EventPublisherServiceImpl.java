/*
 * @ {#} EventPublisherServiceImpl.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services.impl;

import fit.warehouse_service.configs.RabbitMQConfig;
import fit.warehouse_service.events.*;
import fit.warehouse_service.services.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/*
 * @description: Implementation of EventPublisherService to publish events to RabbitMQ.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherServiceImpl implements EventPublisherService {
    private final RabbitTemplate rabbitTemplate;

    private final RabbitMQConfig rabbitMQConfig;

    @Override
    public void publishInstrumentActivated(InstrumentActivatedEvent event) {
        try {
            log.info("Publishing InstrumentActivatedEvent for id: {} | RoutingKey: {}",
                    event.getId(), RabbitMQConfig.INSTRUMENT_ACTIVATED_ROUTING_KEY);

            // Gửi sự kiện đến exchange với routing key cụ thể
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INSTRUMENT_EXCHANGE,
                    RabbitMQConfig.INSTRUMENT_ACTIVATED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish InstrumentActivatedEvent for id: {}. Error: {}",
                    event.getId(), e.getMessage());
        }
    }

    @Override
    public void publishInstrumentDeactivated(InstrumentDeactivatedEvent event) {
        try {
            log.info("Publishing InstrumentDeactivatedEvent for id: {} | RoutingKey: {}",
                    event.getId(), RabbitMQConfig.INSTRUMENT_DEACTIVATED_ROUTING_KEY);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INSTRUMENT_EXCHANGE,
                    RabbitMQConfig.INSTRUMENT_DEACTIVATED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish InstrumentDeactivatedEvent for id: {}. Error: {}",
                    event.getId(), e.getMessage());
        }
    }

    @Override
    public void publishConfigurationCreated(ConfigurationCreatedEvent event) {
        try {
            log.info("Publishing ConfigurationCreatedEvent for id: {} | RoutingKey: {}",
                    event.getId(), RabbitMQConfig.CONFIGURATION_CREATED_ROUTING_KEY);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INSTRUMENT_EXCHANGE,
                    RabbitMQConfig.CONFIGURATION_CREATED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish ConfigurationCreatedEvent for id: {}. Error: {}",
                    event.getId(), e.getMessage());
        }
    }

    @Override
    public void publishConfigurationDeleted(ConfigurationDeletedEvent event) {
        try {
            log.info("Publishing ConfigurationDeletedEvent for id: {} | RoutingKey: {}",
                    event.getConfigurationId(), RabbitMQConfig.CONFIGURATION_DELETED_ROUTING_KEY);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INSTRUMENT_EXCHANGE,
                    RabbitMQConfig.CONFIGURATION_DELETED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish ConfigurationDeletedEvent for id: {}. Error: {}",
                    event.getConfigurationId(), e.getMessage());
        }
    }

    @Override
    public void publishConfigurationUpdated(ConfigurationUpdatedEvent event) {
        try {
            log.info("Publishing ConfigurationUpdatedEvent for id: {} | RoutingKey: {}",
                    event.getId(), RabbitMQConfig.CONFIGURATION_UPDATED_ROUTING_KEY);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INSTRUMENT_EXCHANGE,
                    RabbitMQConfig.CONFIGURATION_UPDATED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish ConfigurationUpdatedEvent for id: {}. Error: {}",
                    event.getId(), e.getMessage());
        }
    }
}
