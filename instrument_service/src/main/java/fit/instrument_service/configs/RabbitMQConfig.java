/*
 * @ {#} RabbitMQConfig.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.configs;

/*
 * @description: Configuration class for RabbitMQ messaging.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String INSTRUMENT_EXCHANGE = "instrument_exchange";

    public static final String INSTRUMENT_ACTIVATED_ROUTING_KEY = "instrument.activated";
    public static final String INSTRUMENT_ACTIVATED_QUEUE = "q.instrument_activated";

    public static final String INSTRUMENT_DEACTIVATED_ROUTING_KEY = "instrument.deactivated";
    public static final String INSTRUMENT_DEACTIVATED_QUEUE = "q.instrument_deactivated";

    public static final String CONFIGURATION_CREATED_ROUTING_KEY = "configuration.created";
    public static final String CONFIGURATION_CREATED_QUEUE = "q.configuration_created";

    public static final String CONFIGURATION_DELETED_ROUTING_KEY = "configuration.deleted";
    public static final String CONFIGURATION_DELETED_QUEUE = "q.configuration_deleted";

    // Constants cho Sync Request
    public static final String SYNC_REQUEST_QUEUE = "instrument.sync.request.queue";
    public static final String SYNC_REQUEST_ROUTING_KEY = "instrument.sync.request";

    // 1. Define Exchange (TopicExchange)
    @Bean
    public TopicExchange instrumentExchange() {
        return new TopicExchange(INSTRUMENT_EXCHANGE);
    }

    // 2. Define Queue
    @Bean
    public Queue syncRequestQueue() {
        return new Queue(SYNC_REQUEST_QUEUE, true);
    }

    // 3. FIX: Sửa tham số từ DirectExchange thành TopicExchange
    @Bean
    public Binding syncRequestBinding(Queue syncRequestQueue, TopicExchange instrumentExchange) {
        return BindingBuilder
                .bind(syncRequestQueue)
                .to(instrumentExchange)
                .with(SYNC_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Queue instrumentActivatedQueue() {
        return new Queue(INSTRUMENT_ACTIVATED_QUEUE, true);
    }

    @Bean
    public Binding instrumentActivatedBinding(Queue instrumentActivatedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder.bind(instrumentActivatedQueue).to(instrumentExchange).with(INSTRUMENT_ACTIVATED_ROUTING_KEY);
    }

    @Bean
    public Queue instrumentDeactivatedQueue() {
        return new Queue(INSTRUMENT_DEACTIVATED_QUEUE, true);
    }

    @Bean
    public Binding instrumentDeactivatedBinding(Queue instrumentDeactivatedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder.bind(instrumentDeactivatedQueue).to(instrumentExchange).with(INSTRUMENT_DEACTIVATED_ROUTING_KEY);
    }

    @Bean
    public Queue configurationCreatedQueue() {
        return new Queue(CONFIGURATION_CREATED_QUEUE, true);
    }

    @Bean
    public Binding configurationCreatedBinding(Queue configurationCreatedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder.bind(configurationCreatedQueue).to(instrumentExchange).with(CONFIGURATION_CREATED_ROUTING_KEY);
    }

    @Bean
    public Queue configurationDeletedQueue() {
        return new Queue(CONFIGURATION_DELETED_QUEUE, true);
    }

    @Bean
    public Binding configurationDeletedBinding(Queue configurationDeletedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder.bind(configurationDeletedQueue).to(instrumentExchange).with(CONFIGURATION_DELETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    public static final String CONFIGURATION_UPDATED_ROUTING_KEY = "configuration.updated";
    public static final String CONFIGURATION_UPDATED_QUEUE = "q.configuration_updated";

    @Bean
    public Queue configurationUpdatedQueue() {
        return new Queue(CONFIGURATION_UPDATED_QUEUE, true);
    }

    @Bean
    public Binding configurationUpdatedBinding(Queue configurationUpdatedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder
                .bind(configurationUpdatedQueue)
                .to(instrumentExchange)
                .with(CONFIGURATION_UPDATED_ROUTING_KEY);
    }
}
