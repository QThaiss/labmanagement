/*
 * @ {#} JwtAuthenticationFilter.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iam_service.dtos.response.ApiResponse;
import fit.iam_service.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
 * @description: Filter to handle JWT authentication for incoming requests
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    private final UserDetailsServiceImpl userDetailsService;

    private final ObjectMapper objectMapper;

    // This method is called for every request to check if the user is authenticated
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = parseJwt(request);

            if (token != null) {

                // Validate the token structure and signature
                if (!jwtUtils.validateStructureAndSignature(token)) {
                    writeError(response, "Invalid or expired token", HttpServletResponse.SC_UNAUTHORIZED, request);
                    return;
                }

                // Check if the token has expired
                if (jwtUtils.isExpired(token)) {
                    writeError(response, "Token has expired", HttpServletResponse.SC_UNAUTHORIZED, request);
                    return;
                }

                // Check if the token type is "access"
                String typ = jwtUtils.getType(token);
                if (!"access".equals(typ)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Get username from the token and set authentication in the context
                String username = jwtUtils.getUsernameClaim(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Create authentication token and set it in the security context
                    UsernamePasswordAuthenticationToken  auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication", e);
        }

        filterChain.doFilter(request, response);
    }

    // Writes an error response in JSON format
    private void writeError(HttpServletResponse response, String message, int status, HttpServletRequest request) throws IOException {
        ApiResponse<Object> apiResponse = ApiResponse.error(status, message, request.getRequestURI());

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    // Parses the JWT from the Authorization header
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
