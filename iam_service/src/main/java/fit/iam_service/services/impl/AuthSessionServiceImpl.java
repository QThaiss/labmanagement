/*
 * @ {#} AuthSessionServiceImpl.java   1.0     02/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.services.impl;

import fit.iam_service.entities.AuthSession;
import fit.iam_service.entities.User;
import fit.iam_service.exceptions.NotFoundException;
import fit.iam_service.repositories.AuthSessionRepository;
import fit.iam_service.services.AuthSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/*
 * @description: Service implementation for managing authentication sessions
 * @author: Tran Hien Vinh
 * @date:   02/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthSessionServiceImpl implements AuthSessionService {
    private final AuthSessionRepository authSessionRepository;

    private static final int MAX_SESSIONS = 3;
    @Override
    @Transactional
    public AuthSession createSession(User user, String jti, String refreshTokenHash, String ip, LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // Delete expired or revoked sessions
        authSessionRepository.deleteExpiredAndRevokedSessions(now, now);

        // Get list of active sessions
        List<AuthSession> activeSessions = authSessionRepository
                .findByUserAndRevokedAtIsNullAndExpiresAtAfter(user, now);

        // If the user has reached the maximum number of sessions, revoke the oldest one
        if (activeSessions.size() >= MAX_SESSIONS) {
            activeSessions.stream()
                    .min(Comparator.comparing(AuthSession::getIssuedAt))
                    .ifPresent(oldest -> {
                        oldest.setRevokedAt(now);
                        authSessionRepository.save(oldest);
                    });
        }

        // Create and save the new session
        AuthSession session = AuthSession.builder()
                .user(user)
                .jti(jti)
                .refreshTokenHash(refreshTokenHash)
                .ip(ip)
                .expiresAt(expiresAt)
                .build();

        return authSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void revokeSession(String jti) {
        authSessionRepository.revokeSessionByJti(jti, LocalDateTime.now(ZoneOffset.UTC));
    }

    @Override
    @Transactional
    public void revokeAllUserSessions(User user) {
        authSessionRepository.revokeAllActiveSessionsByUser(user, LocalDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public Optional<AuthSession> findActiveSessionByJti(String jti) {
        return authSessionRepository.findByJti(jti)
                .filter(AuthSession::isActive);
    }

    @Override
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now(ZoneOffset.UTC).minusDays(30);
        authSessionRepository.deleteExpiredAndRevokedSessions(
                LocalDateTime.now(ZoneOffset.UTC),
                cutoffTime
        );
    }

    @Override
    public AuthSession findByJti(String jti) {
        return authSessionRepository.findByJti(jti).orElseThrow(() -> new NotFoundException("Session not found"));
    }

    @Override
    public void revoke(AuthSession session) {
        session.setRevokedAt(LocalDateTime.now(ZoneOffset.UTC));
        authSessionRepository.save(session);
    }

    @Override
    public void revokeAllForUser(User user) {
        authSessionRepository.revokeAllActiveSessionsByUser(user, LocalDateTime.now(ZoneOffset.UTC));
    }
}
