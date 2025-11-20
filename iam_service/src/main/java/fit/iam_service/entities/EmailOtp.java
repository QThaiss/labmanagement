/*
 * @ (#) EmailOtp.java    1.0    06/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 06/10/2025
 * @version: 1.0
 */

import fit.iam_service.enums.DeletedReason;
import fit.iam_service.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(
        name = "email_otp",
        indexes = {
                @Index(name = "idx_emailotp_expires", columnList = "expires_at"),
                @Index(name = "idx_emailotp_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailOtp {

    @Id
    @Column(name = "otp_id")
    private String otpId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_emailotp_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 32)
    private OtpPurpose purpose;

    // LƯU HASH, không lưu OTP thô
    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Soft-delete & trạng thái tiêu thụ/thu hồi ---
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "deleted_reason", length = 32)
    private DeletedReason deletedReason; // EXPIRED | ATTEMPTS_EXCEEDED | CONSUMED | HARD_DELETE | REVOKED

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt; // khi verify thành công

    @PrePersist
    void prePersist() {
        if (otpId == null) otpId = UUID.randomUUID().toString();
        if (attempts == null) attempts = 0;
        if (maxAttempts == null) maxAttempts = 5;
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
