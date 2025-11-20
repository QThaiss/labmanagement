/*
 * @ {#} AuthSessionService.java   1.0     02/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.services;

import fit.iam_service.entities.AuthSession;
import fit.iam_service.entities.User;

import java.time.LocalDateTime;
import java.util.Optional;

/*
 * @description: Service interface for managing authentication sessions
 * @author: Tran Hien Vinh
 * @date:   02/10/2025
 * @version:    1.0
 */
public interface AuthSessionService {
    AuthSession createSession(User user, String jti, String refreshTokenHash, String ip, LocalDateTime expiresAt);

    void revokeSession(String jti);

    void revokeAllUserSessions(User user);

    Optional<AuthSession> findActiveSessionByJti(String jti);

    void cleanupExpiredSessions();

    AuthSession findByJti(String jti);

    void revoke(AuthSession session);

    void revokeAllForUser(User user);
}
