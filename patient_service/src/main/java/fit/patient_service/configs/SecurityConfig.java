/*
 * @ {#} SecurityConfig.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.configs;

import fit.patient_service.security.CustomAccessDeniedHandler;
import fit.patient_service.security.CustomAuthEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/*
 * @description: Security configuration class for the application
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${jwt.signed-key}")
    private String secret;
    @Value("${jwt.issuer}")
    private String issuer;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CustomAuthEntryPoint customAuthEntryPoint, CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(customAuthEntryPoint)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint) // Exception for unauthenticated users
                        .accessDeniedHandler(customAccessDeniedHandler)); // Exception for authenticated but unauthorized users
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        // Decode the Base64-encoded secret key
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");

        // Create a decoder NimbusJwtDecoder that uses that key to verify JWT
        NimbusJwtDecoder dec = NimbusJwtDecoder.withSecretKey(key).build();

        // Set the expected issuer to validate the "iss" claim
        dec.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return dec;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Custom converter to extract roles and privileges from JWT claims
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();

            // Role
            String role = jwt.getClaimAsString("role");
            if (role != null && !role.isBlank()) {
                // đảm bảo có prefix ROLE_
                String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                authorities.add(new SimpleGrantedAuthority(normalized));
            }

            // Privileges
            List<String> privileges = jwt.getClaimAsStringList("privileges");
            if (privileges != null && !privileges.isEmpty()) {
                privileges.forEach(priv -> authorities.add(new SimpleGrantedAuthority(priv)));
            }

            return authorities;
        });

        return converter;
    }
}

