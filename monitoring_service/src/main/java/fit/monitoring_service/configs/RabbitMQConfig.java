/*
 * @ (#) RabbitMQConfig.java    1.0    24/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.configs;/*
 * @description:
 * @author: Bao Thong
 * @date: 24/11/2025
 * @version: 1.0
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange chung của hệ thống Instrument
    public static final String INSTRUMENT_EXCHANGE = "instrument_exchange";

    // Queue dành riêng cho Monitoring Service để backup kết quả
    public static final String TEST_RESULT_BACKUP_QUEUE = "q.monitoring.test_result_backup";

    // Routing key lắng nghe sự kiện có kết quả xét nghiệm
    public static final String TEST_RESULT_ROUTING_KEY = "instrument.test_result";

    // Lấy giá trị từ application.properties, nếu không có thì dùng mặc định "event_log_queue"
    @Value("${rabbitmq.queue.event-log:event_log_queue}")
    private String eventLogQueueName;

    @Bean
    public TopicExchange instrumentExchange() {
        return new TopicExchange(INSTRUMENT_EXCHANGE);
    }

    @Bean
    public Queue testResultBackupQueue() {
        // durable = true để đảm bảo hàng đợi không bị mất khi RabbitMQ restart
        return new Queue(TEST_RESULT_BACKUP_QUEUE, true);
    }

    @Bean
    public Queue eventLogQueue() {
        // Tạo queue bền vững (durable) để lưu trữ log sự kiện
        return new Queue(eventLogQueueName, true);
    }

    @Bean
    public Binding testResultBackupBinding(Queue testResultBackupQueue, TopicExchange instrumentExchange) {
        return BindingBuilder
                .bind(testResultBackupQueue)
                .to(instrumentExchange)
                .with(TEST_RESULT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Hỗ trợ Java 8 Time API
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
