/*
 * @ {#} CustomAccessDeniedHandler.java   1.0     07/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fit.iam_service.dtos.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
 * @description: Custom handler for access denied exceptions in Spring Security
 * @author: Tran Hien Vinh
 * @date:   07/10/2025
 * @version:    1.0
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    public CustomAccessDeniedHandler() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        // Create a standardized error response
        ApiResponse<Object> apiResponse = ApiResponse.error(
                HttpServletResponse.SC_FORBIDDEN,
                "You do not have permission to access this resource.",
                request.getRequestURI()
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
