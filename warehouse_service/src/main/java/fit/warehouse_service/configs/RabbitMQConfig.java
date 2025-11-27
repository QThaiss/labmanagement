/*
 * @ {#} RabbitMQConfig.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @description: Cấu hình RabbitMQ cho warehouse-service
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Configuration
public class RabbitMQConfig {
    // Định nghĩa tên Exchange chung cho nghiệp vụ instrument
    public static final String INSTRUMENT_EXCHANGE = "instrument_exchange";

    // Định nghĩa Routing Key cho sự kiện "activated"
    public static final String INSTRUMENT_ACTIVATED_ROUTING_KEY = "instrument.activated";

    // Định nghĩa Queue mà instrument-service sẽ lắng nghe
    public static final String INSTRUMENT_ACTIVATED_QUEUE = "q.instrument_activated";

    // Định nghĩa Routing Key cho sự kiện "deactivated"
    public static final String INSTRUMENT_DEACTIVATED_ROUTING_KEY = "instrument.deactivated";

    // Định nghĩa Queue mà instrument-service sẽ lắng nghe
    public static final String INSTRUMENT_DEACTIVATED_QUEUE = "q.instrument_deactivated";

    // --- Hằng số cho Configuration Created ---
    public static final String CONFIGURATION_CREATED_ROUTING_KEY = "configuration.created";
    public static final String CONFIGURATION_CREATED_QUEUE = "q.configuration_created";

    // --- Hằng số cho Configuration Deleted ---
    public static final String CONFIGURATION_DELETED_ROUTING_KEY = "configuration.deleted";
    public static final String CONFIGURATION_DELETED_QUEUE = "q.configuration_deleted";

    // Thêm các hằng số mới
    public static final String CONFIGURATION_UPDATED_ROUTING_KEY = "configuration.updated";
    public static final String CONFIGURATION_UPDATED_QUEUE = "q.configuration_updated";

    // Thêm Bean Queue
    @Bean
    public Queue configurationUpdatedQueue() {
        return new Queue(CONFIGURATION_UPDATED_QUEUE, true);
    }

    // Thêm Bean Binding
    @Bean
    public Binding configurationUpdatedBinding(Queue configurationUpdatedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder
                .bind(configurationUpdatedQueue)
                .to(instrumentExchange)
                .with(CONFIGURATION_UPDATED_ROUTING_KEY);
    }

    @Bean
    public TopicExchange instrumentExchange() {
        return new TopicExchange(INSTRUMENT_EXCHANGE);
    }

    // Bean này tạo ra Queue (hàng đợi)
    @Bean
    public Queue instrumentActivatedQueue() {
        return new Queue(INSTRUMENT_ACTIVATED_QUEUE, true);
    }

    // Bean này liên kết (bind) Queue với Exchange thông qua Routing Key
    @Bean
    public Binding instrumentActivatedBinding(Queue instrumentActivatedQueue, TopicExchange instrumentExchange) {
        return BindingBuilder
                .bind(instrumentActivatedQueue)
                .to(instrumentExchange)
                .with(INSTRUMENT_ACTIVATED_ROUTING_KEY);
    }

    // Bean này tạo ra Queue (hàng đợi)
    @Bean
    public Queue instrumentDeactivatedQueue() {
        return new Queue(INSTRUMENT_DEACTIVATED_QUEUE, true);
    }

    // Bean này liên kết (bind) Queue với Exchange thông qua Routing Key
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

    // Bean này cấu hình Message Converter sử dụng Jackson để chuyển đổi JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
