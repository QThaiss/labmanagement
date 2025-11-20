/*
 * @ {#} JwtUtils.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.security.jwt;

import fit.iam_service.security.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/*
 * @description: Utility class for handling JWT operations
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
@Component
@Slf4j
public class JwtUtils {
    @Value("${jwt.signed-key}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long accessTtlMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshTtlMs;

    @Value("${jwt.issuer:iam-service}")
    private String issuer;

    @Value("${jwt.audience:all-services}")
    private String audience;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Generates an access token for the user
    public String generateAccessToken(UserDetailsImpl principal) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTtlMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("typ", "access");
        claims.put("uname", principal.getUsername());

        if (principal.getRole() != null) {
            claims.put("role", principal.getRole());
        }

        if (principal.getPrivileges() != null && !principal.getPrivileges().isEmpty()) {
            claims.put("privileges", principal.getPrivileges());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(principal.getId())             // sub = userId
                .setIssuer(issuer)                         // iss
                .setAudience(audience)                     // aud
                .setId(UUID.randomUUID().toString())       // jti
                .setIssuedAt(now)                          // iat
                .setExpiration(exp)                        // exp
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Generates a refresh token for the user
    public String generateRefreshToken(UserDetailsImpl principal) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTtlMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("typ", "refresh");
        claims.put("uname", principal.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(principal.getId())
                .setIssuer(issuer)
                .setAudience(issuer)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Parses the JWT token and returns the claims
    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Retrieves the username claim from the JWT token
    public String getUsernameClaim(String token) {
        Object v = parse(token).get("uname");
        return v == null ? null : String.valueOf(v);
    }

    // Retrieves the type claim from the JWT token
    public String getType(String token) {
        Object v = parse(token).get("typ");
        return v == null ? null : String.valueOf(v);
    }

    // Retrieves the expiration date from the JWT token
    public Date getExpiration(String token) {
        return parse(token).getExpiration();
    }

    // Checks if the JWT token is expired
    public boolean isExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    // Validates the structure and signature of the JWT token
    public boolean validateStructureAndSignature(String token) {
        try {
            parse(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT invalid", e.getMessage());
        }
        return false;
    }

    // Retrieves the JWT ID (jti) from the JWT token
    public String getJtiFromToken(String token) {
        return parse(token).getId();
    }

    // Retrieves the expiration date from the JWT token as LocalDateTime
    public LocalDateTime getExpirationFromToken(String token) {
        Date expiration = getExpiration(token);
        return expiration.toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
    }
}
