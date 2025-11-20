/*
 * @ (#) ReportNotification.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "report_notification", indexes = {
        @Index(name = "idx_notify_user_time", columnList = "user_id, created_at DESC"),
        @Index(name = "idx_notify_job", columnList = "job_id"),
        @Index(name = "idx_notify_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportNotification {
    @Id
    @Column(name = "notify_id", length = 36, nullable = false, updatable = false)
    private String notifyId;

    @NotBlank
    @Column(name = "job_id", length = 36, nullable = false)
    private String jobId;

    @NotBlank
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 16, nullable = false)
    private NotificationChannel channel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private NotificationStatus status;

    @Size(max = 128)
    @Column(name = "title", length = 128)
    private String title;

    @Size(max = 512)
    @Column(name = "content", length = 512)
    private String content;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at", columnDefinition = "datetime(6)")
    private LocalDateTime sentAt;

    public enum NotificationChannel {IN_APP, EMAIL, WEBHOOK}

    public enum NotificationStatus {PENDING, SENT, FAILED}

    /* Relation */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", referencedColumnName = "job_id",
            foreignKey = @ForeignKey(name = "fk_notify_job"), insertable = false, updatable = false)
    private ReportJob jobRef;

    @PrePersist
    void pp() {
        if (notifyId == null) notifyId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}