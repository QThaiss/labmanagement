/*
 * @ {#} CustomAuthEntryPoint.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.patient_service.dtos.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
 * @description: Custom authentication entry point to handle unauthorized access attempts
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getOutputStream(), ApiResponse.unauthorized(authException.getMessage(), request.getServletPath()));
    }
}
