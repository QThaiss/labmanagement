/*
 * @ (#) AuthSession.java    1.0    01/10/2025
 */
package fit.iam_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(
        name = "auth_sessions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_auth_sessions_jti", columnNames = "jti")
        },
        indexes = {
                @Index(name = "idx_auth_sessions_user_expires", columnList = "user_id, expires_at"),
                @Index(name = "idx_auth_sessions_revoked_at", columnList = "revoked_at")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthSession {

    @Id
    @Column(name = "session_id", length = 36, nullable = false, updatable = false)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_auth_session_user"))
    private User user;

    // JWT ID hoặc mã định danh phiên
    @Column(name = "jti", length = 128, nullable = false, unique = true)
    private String jti;

    // Lưu HASH của refresh token (không lưu token thô)
    @Column(name = "refresh_token_hash", length = 255, nullable = false)
    private String refreshTokenHash;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "ip", length = 45)
    private String ip;

    @PrePersist
    void prePersist() {
        if (this.sessionId == null) this.sessionId = UUID.randomUUID().toString();
        if (this.issuedAt == null) this.issuedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public boolean isExpired() {
        return LocalDateTime.now(ZoneOffset.UTC).isAfter(this.expiresAt);
    }

    public boolean isRevoked() {
        return this.revokedAt != null;
    }

    public boolean isActive() {
        return !isExpired() && !isRevoked();
    }
}