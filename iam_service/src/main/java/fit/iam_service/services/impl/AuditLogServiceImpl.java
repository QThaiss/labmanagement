/*
 * @ {#} AuditLogServiceImpl.java   1.0     02/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iam_service.entities.AuditLog;
import fit.iam_service.entities.User;
import fit.iam_service.enums.AuditAction;
import fit.iam_service.repositories.AuditLogRepository;
import fit.iam_service.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/*
 * @description: Service implementation for managing audit logs
 * @author: Tran Hien Vinh
 * @date:   02/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logUserLogin(User user, String ip) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> details = Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "loginTime", LocalDateTime.now(ZoneOffset.UTC).toString()
            );

            String detailsJson = mapper.writeValueAsString(details);
            AuditLog auditLog = AuditLog.builder()
                    .actorId(user.getUserId())
                    .targetId(user.getUserId())
                    .action(AuditAction.LOGIN)
                    .detailsJson(detailsJson)
                    .ip(ip)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log for login", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void logRefreshToken(User user, String ip, String oldJti, String newJti) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> details = Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "oldJti", oldJti,
                    "newJti", newJti,
                    "refreshTime", LocalDateTime.now(ZoneOffset.UTC).toString()
            );

            String detailsJson = mapper.writeValueAsString(details);
            AuditLog auditLog = AuditLog.builder()
                    .actorId(user.getUserId())
                    .targetId(user.getUserId())
                    .action(AuditAction.REFRESH_TOKEN)
                    .detailsJson(detailsJson)
                    .ip(ip)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log for refresh token", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logForgotPassword(User user, String ip) {
        try {
            if (user == null) return;
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> details = Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "requestTime", LocalDateTime.now(ZoneOffset.UTC).toString()
            );

            String detailsJson = mapper.writeValueAsString(details);
            AuditLog log = AuditLog.builder()
                    .actorId(user.getUserId())
                    .targetId(user.getUserId())
                    .action(AuditAction.FORGOT_PASSWORD)
                    .ip(ip)
                    .detailsJson(detailsJson)
                    .build();
            auditLogRepository.save(log);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log for forgot password", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logPasswordReset(User user, String ip) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> details = Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "resetTime", LocalDateTime.now(ZoneOffset.UTC).toString()
            );

            String detailsJson = mapper.writeValueAsString(details);
            AuditLog auditLog = AuditLog.builder()
                    .actorId(user.getUserId())
                    .targetId(user.getUserId())
                    .action(AuditAction.RESET_PASSWORD)
                    .detailsJson(detailsJson)
                    .ip(ip)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log for password reset", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logUserLogout(User user, String ip, String jti, boolean allSessions) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> details = Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "jti", jti == null ? "N/A" : jti,
                    "allSessions", allSessions,
                    "logoutTime", LocalDateTime.now(ZoneOffset.UTC).toString()
            );

            String detailsJson = mapper.writeValueAsString(details);
            AuditLog auditLog = AuditLog.builder()
                    .actorId(user.getUserId())
                    .targetId(user.getUserId())
                    .action(AuditAction.LOGOUT)
                    .detailsJson(detailsJson)
                    .ip(ip)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log for logout", e);
            throw new RuntimeException(e);
        }
    }
}
