/*
 * @ {#} GatewayOnlyFilter.java   1.0     02/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
 * @description: Filter to allow access only through the internal gateway or from other services
 * @author: Tran Hien Vinh
 * @date:   02/10/2025
 * @version:    1.0
 */
@Component
@Slf4j
public class GatewayOnlyFilter extends OncePerRequestFilter {

    @Value("${internal.gateway.key}")
    private String gatewayKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator") || path.startsWith("/eureka");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String gatewayHeader = request.getHeader("X-Internal-Gateway");
        String serviceHeader = request.getHeader("X-Service-Call");

        boolean fromGateway = gatewayKey.equals(gatewayHeader);

        boolean fromService = serviceHeader != null && !serviceHeader.isBlank();

        if (fromGateway || fromService) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
              {"success":false,"message":"Forbidden: Must go through gateway","status":403,"path":"%s"}
              """.formatted(request.getServletPath()));
    }
}

