/*
 * @ {#} AuthEntryPointJwt.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iam_service.dtos.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
 * @description: This class implements the AuthenticationEntryPoint interface to handle unauthorized access attempts
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), ApiResponse.unauthorized(authException.getMessage()));
    }
}
