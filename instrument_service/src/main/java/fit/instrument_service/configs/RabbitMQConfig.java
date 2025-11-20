/*
 * @ {#} RabbitMQConfig.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @description: Configuration class for RabbitMQ messaging.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Configuration
public class RabbitMQConfig {
    public static final String INSTRUMENT_EXCHANGE = "instrument_exchange";

    public static final String INSTRUMENT_ACTIVATED_ROUTING_KEY = "instrument.activated";
    public static final String INSTRUMENT_ACTIVATED_QUEUE = "q.instrument_activated";

    public static final String INSTRUMENT_DEACTIVATED_ROUTING_KEY = "instrument.deactivated";
    public static final String INSTRUMENT_DEACTIVATED_QUEUE = "q.instrument_deactivated";

    // --- hằng số cho Configuration Created ---
    public static final String CONFIGURATION_CREATED_ROUTING_KEY = "configuration.created";
    public static final String CONFIGURATION_CREATED_QUEUE = "q.configuration_created";
    // ---------------------------------------------

    public static final String CONFIGURATION_DELETED_ROUTING_KEY = "configuration.deleted";
    public static final String CONFIGURATION_DELETED_QUEUE = "q.configuration_deleted";

    @Bean
    public TopicExchange instrumentExchange() {
        return new TopicExchange(INSTRUMENT_EXCHANGE);
    }

    @Bean
    public Queue instrumentActivatedQueue() {
        return new Queue(INSTRUMENT_ACTIVATED_QUEUE, true);
    }

    @Bean
    public Binding instrumentActivatedBinding(Queue instrumentActivatedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder
                .bind(instrumentActivatedQueue)
                .to(instrumentExchange)
                .with(INSTRUMENT_ACTIVATED_ROUTING_KEY);
    }

    @Bean
    public Queue instrumentDeactivatedQueue() {
        return new Queue(INSTRUMENT_DEACTIVATED_QUEUE, true);
    }

    @Bean
    public Binding instrumentDeactivatedBinding(Queue instrumentDeactivatedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder
                .bind(instrumentDeactivatedQueue)
                .to(instrumentExchange)
                .with(INSTRUMENT_DEACTIVATED_ROUTING_KEY);
    }

    // --- @Bean cho Configuration Created ---
    @Bean
    public Queue configurationCreatedQueue() {
        return new Queue(CONFIGURATION_CREATED_QUEUE, true);
    }

    @Bean
    public Binding configurationCreatedBinding(Queue configurationCreatedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder
                .bind(configurationCreatedQueue)
                .to(instrumentExchange)
                .with(CONFIGURATION_CREATED_ROUTING_KEY);
    }
    // --------------------------------------------

    // --- @Bean cho Configuration Deleted ---
    @Bean
    public Queue configurationDeletedQueue() {
        return new Queue(CONFIGURATION_DELETED_QUEUE, true);
    }

    @Bean
    public Binding configurationDeletedBinding(Queue configurationDeletedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder
                .bind(configurationDeletedQueue)
                .to(instrumentExchange)
                .with(CONFIGURATION_DELETED_ROUTING_KEY);
    }
    // --------------------------------------------

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
