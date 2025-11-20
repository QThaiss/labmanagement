/*
 * @ (#) OrderEventLog.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.EventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "order_event_log", indexes = {
        @Index(name = "idx_evt_order_time", columnList = "order_id, created_at DESC"),
        @Index(name = "idx_evt_type_time", columnList = "event_type, created_at DESC"),
        @Index(name = "idx_evt_actor_time", columnList = "actor_user_id, created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEventLog {
    @Id
    @Column(name = "event_id", length = 36, nullable = false, updatable = false)
    private String eventId;

    @NotBlank
    @Column(name = "order_id", length = 36, nullable = false)
    private String orderId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 32, nullable = false)
    private EventType eventType;

    @NotBlank
    @Column(name = "actor_user_id", length = 36, nullable = false)
    private String actorUserId;

    @Lob
    @Column(name = "details")
    private String details;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_json", columnDefinition = "json")
    private String beforeJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_json", columnDefinition = "json")
    private String afterJson;

    @Size(max = 64)
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Size(max = 255)
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime createdAt;

    /* Relations */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id",
            foreignKey = @ForeignKey(name = "fk_evt_order"), insertable = false, updatable = false)
    private TestOrder orderRef;

    @PrePersist
    void pp() {
        if (eventId == null) eventId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}