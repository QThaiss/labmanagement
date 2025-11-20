/*
 * @ (#) SecurityConfig.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.configs;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import fit.iam_service.security.AuthEntryPointJwt;
import fit.iam_service.security.CustomAccessDeniedHandler;
import fit.iam_service.security.CustomAuthenticationProvider;
import fit.iam_service.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthEntryPointJwt unauthorizedHandler;

    private final CustomAccessDeniedHandler accessDeniedHandler;

    private final CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(customAuthenticationProvider);
        return authBuilder.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return customAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler) )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(customAuthenticationProvider)
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/iam/auth/login",
                                "/api/v1/iam/roles/**",
                                "/api/v1/iam/users",
                                "/api/v1/iam/users/email/verify",
                                "/api/v1/iam/users/email/resend",
                                "/api/v1/iam/auth/refresh-token",
                                "/api/v1/iam/auth/forgot-password",
                                "/api/v1/iam/auth/reset-password",
                                "/api/v1/iam/auth/encrypt"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/iam/auth/password/**",
                                "/api/v1/iam/auth/public-key"
                        ).permitAll()
//                        .requestMatchers(HttpMethod.PUT, "/api/v1/iam/users/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/v1/iam/users/**").permitAll()
//                        .requestMatchers(HttpMethod.DELETE, "/api/v1/iam/users/**").permitAll()
                        .anyRequest().authenticated()
                );

        // Add JWT authentication filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
