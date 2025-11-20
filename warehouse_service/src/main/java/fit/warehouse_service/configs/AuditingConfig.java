/*
 * @ (#) AuditingConfig.java    1.0    27/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.configs;/*
 * @description:
 * @author: Bao Thong
 * @date: 27/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.exceptions.UnauthorizedException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@Configuration
public class AuditingConfig {

    /**
     * Cung cấp logic để JPA biết "ai" là người thực hiện hành động (CreatedBy, LastModifiedBy).
     * trích xuất userId từ 'subject' (sub) của JWT.
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || !(authentication.getPrincipal() instanceof Jwt jwt)) {
                throw new UnauthorizedException("No authenticated user found");
            }
            return Optional.of(jwt.getSubject());
        };
    }
}
