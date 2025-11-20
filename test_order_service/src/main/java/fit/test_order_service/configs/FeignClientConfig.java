/*
 * @ {#} FeignClientConfig.java   1.0     07/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.configs;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
public class FeignClientConfig {
    @Value("${internal.gateway.key}")
    private String internalKey;

    @Value("${spring.application.name}")
    private String serviceName;

    // Interceptor to add custom headers to each Feign request
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("X-Service-Call", serviceName);
                template.header("X-Internal-Gateway", internalKey);

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                    String token = jwt.getTokenValue();
                    template.header("Authorization", "Bearer " + token);
                }
            }
        };
    }

    // Custom error decoder to handle errors from Feign clients
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
