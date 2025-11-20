/*
 * @ {#} SecurityConfig.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.gateway_service.configs;


import fit.gateway_service.security.CustomAuthEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

/*
 * @description: Security configuration class for the application
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Value("${jwt.signed-key}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, CustomAuthEntryPoint customAuthEntryPoint) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.POST,
                                "/api/v1/iam/auth/login",
                                "/api/v1/iam/users",
                                "/api/v1/iam/users/email/verify",
                                "/api/v1/iam/users/email/resend",
                                "/api/v1/iam/roles/**",
                                "/api/v1/iam/auth/refresh-token",
                                "/api/v1/iam/auth/forgot-password",
                                "/api/v1/iam/auth/reset-password",
                                "/api/v1/iam/auth/encrypt"
                        ).permitAll()
                        .pathMatchers(HttpMethod.GET,
                                "/api/v1/iam/auth/password/**",
                                "/api/v1/iam/auth/public-key"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthEntryPoint))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(jwtSecret), "HmacSHA256");
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3001"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache preflight request

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
