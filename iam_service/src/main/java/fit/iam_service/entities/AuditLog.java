/*
 * @ (#) AuditLog.java    1.0    30/09/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 30/09/2025
 * @version: 1.0
 */

import fit.iam_service.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(
        name = "audit_log",
        indexes = {
                @Index(name = "idx_audit_target_time", columnList = "target_id, created_at DESC"),
                @Index(name = "idx_audit_actor_time", columnList = "actor_id, created_at DESC")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @Column(name = "audit_id", length = 36, nullable = false, updatable = false)
    private String auditId;

    // người thực hiện hành động (có thể là admin hoặc chính user)
    @Column(name = "actor_id", length = 36)
    private String actorId;

    // đối tượng bị tác động (user_id)
    @Column(name = "target_id", length = 36)
    private String targetId;

    @Column(name = "entity", length = 64, nullable = false)
    @Builder.Default
    private String entity = "USER";

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 32, nullable = false)
    private AuditAction action;

    // Lưu diff/fields JSON: {"before": {...}, "after": {...}}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details")
    private String detailsJson;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.auditId == null) this.auditId = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
