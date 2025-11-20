/*
 * @ (#) PasswordResetRequest.java    1.0    01/10/2025
 */
package fit.iam_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(
        name = "password_reset_requests",
        indexes = {
                @Index(name = "idx_prr_user_expires", columnList = "user_id, expires_at")
        }
        // Có thể thêm unique tokenHash nếu policy yêu cầu: UNIQUE(token_hash)
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PasswordResetRequest {

    @Id
    @Column(name = "request_id", length = 36, nullable = false, updatable = false)
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_prr_user"))
    private User user;

    // snapshot email tại thời điểm yêu cầu
    @Column(name = "email", length = 255, nullable = false)
    private String email;

    // Lưu HASH của reset token (không lưu token thô)
    @Column(name = "token_hash", length = 255, nullable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "requested_ip", length = 45)
    private String requestedIp;

    @Column(name = "requested_user_agent", length = 255)
    private String requestedUserAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.requestId == null) this.requestId = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}