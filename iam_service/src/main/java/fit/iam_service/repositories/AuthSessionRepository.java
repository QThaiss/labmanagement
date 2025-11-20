/*
 * @ {#} AuthSessionRepository.java   1.0     02/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.repositories;

import fit.iam_service.entities.AuthSession;
import fit.iam_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for managing AuthSession entities
 * @author: Tran Hien Vinh
 * @date:   02/10/2025
 * @version:    1.0
 */
@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {

    Optional<AuthSession> findByJti(String jti);

    List<AuthSession> findByUserAndRevokedAtIsNullAndExpiresAtAfter(User user, LocalDateTime now);

    @Modifying
    @Query("UPDATE AuthSession s SET s.revokedAt = :revokedAt WHERE s.user = :user AND s.revokedAt IS NULL")
    void revokeAllActiveSessionsByUser(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);

    @Modifying
    @Query("UPDATE AuthSession s SET s.revokedAt = :revokedAt WHERE s.jti = :jti")
    void revokeSessionByJti(@Param("jti") String jti, @Param("revokedAt") LocalDateTime revokedAt);

    @Modifying
    @Query("DELETE FROM AuthSession s WHERE s.expiresAt < :expiredBefore OR s.revokedAt < :revokedBefore")
    void deleteExpiredAndRevokedSessions(@Param("expiredBefore") LocalDateTime expiredBefore, @Param("revokedBefore") LocalDateTime revokedBefore);

    // NEW: hard-delete all sessions of a user
    @Modifying
    @Query("DELETE FROM AuthSession s WHERE s.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}
