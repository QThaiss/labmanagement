/*
 * @ {#} GlobalExceptionHandler.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.gateway_service.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/*
 * @description: Global exception handler for handling exceptions in a Spring Cloud Gateway application
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */
@Component
@Order(-2) // Ensure this has higher precedence than default handlers
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Handle connection errors, internal errors, fallback JSON
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal Server Error";

        if (ex.getCause() instanceof ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service temporarily unavailable. Please try again later.";
        }

        if (ex.getMessage() != null && ex.getMessage().contains("Read timed out")) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "Downstream service timeout. Please try again.";
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            bytes = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
