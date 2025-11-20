/*
 * @ {#} CustomAuthEntryPoint.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.gateway_service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/*
 * @description: Custom authentication entry point to handle unauthorized access
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
@Component
@Slf4j
public class CustomAuthEntryPoint implements ServerAuthenticationEntryPoint {

    // Unauthorized / expired token handling
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String message = "Unauthorized";

        // Customize message based on the exception type
        if (e instanceof InvalidBearerTokenException &&
                e.getCause() instanceof JwtValidationException cause) {

            if (cause.getMessage() != null && cause.getMessage().contains("Jwt expired")) {
                message = "Token has expired";
            } else {
                message = "Invalid or malformed token";
            }
        }

        // Log the exception message
        log.error("Authentication failed: {}", e.getMessage(), e);

        String body = """
                {
                  "success": false,
                  "message": "%s",
                  "timestamp": "%s",
                  "status": 401,
                  "path": "%s"
                }
                """.formatted(
                message,
                LocalDateTime.now(),
                exchange.getRequest().getPath()
        );

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }
}
