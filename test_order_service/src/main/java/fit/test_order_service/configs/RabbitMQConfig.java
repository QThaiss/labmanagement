/*
 * @ {#} RabbitMQConfig.java   1.0     13/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   13/11/2025
 * @version:    1.0
 */
@Configuration
public class RabbitMQConfig {

    // Định nghĩa tên
    public static final String EXCHANGE_NAME = "instrument_exchange";
    public static final String QUEUE_NAME = "q.order_submitted_to_instrument";
    public static final String ROUTING_KEY = "r.order.submitted";

    @Bean
    public TopicExchange instrumentExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue orderSubmittedQueue() {
        // durable = true (Queue sẽ tồn tại sau khi RabbitMQ restart)
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    // Cấu hình để RabbitTemplate gửi/nhận JSON
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        // Cần đăng ký JavaTimeModule để serialize/deserialize LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
