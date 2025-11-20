/*
 * @ (#) PasswordHistory.java    1.0    30/09/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 30/09/2025
 * @version: 1.0
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(
        name = "password_history",
        indexes = {
                @Index(name = "idx_ph_user_changed_at", columnList = "user_id, changed_at DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordHistory {

    @Id
    @Column(name = "history_id", length = 36, nullable = false, updatable = false)
    private String historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ph_user"))
    private User user;

    @NotBlank
    @Size(max = 255)
    @Column(name = "old_password_hash", length = 255, nullable = false)
    private String oldPasswordHash;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    // ai đổi (user tự đổi hoặc admin), lưu id dạng String
    @Column(name = "changed_by", length = 36)
    private String changedBy;

    @PrePersist
    void prePersist() {
        if (this.historyId == null) this.historyId = UUID.randomUUID().toString();
        if (this.changedAt == null) this.changedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
