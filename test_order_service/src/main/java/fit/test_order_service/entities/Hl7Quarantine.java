/*
 * @ (#) Hl7Quarantine.java    1.0    11/10/2025
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
@Table(name = "hl7_quarantine", indexes = {
        @Index(name = "idx_q_msg", columnList = "message_id"),
        @Index(name = "idx_q_raw", columnList = "raw_id"),
        @Index(name = "idx_q_time", columnList = "quarantined_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hl7Quarantine {
    @Id
    @Column(name = "q_id", length = 36, nullable = false, updatable = false)
    private String qId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "message_id", length = 64, nullable = false)
    private String messageId;

    @Column(name = "raw_id", length = 36)
    private String rawId;

    @NotBlank
    @Size(max = 256)
    @Column(name = "reason", length = 256, nullable = false)
    private String reason;

    @Lob
    @Column(name = "details")
    private String details;

    @Column(name = "quarantined_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime quarantinedAt;

    @Column(name = "resolved_at", columnDefinition = "datetime(6)")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by", length = 36)
    private String resolvedBy;

    /* Relation */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_id", referencedColumnName = "raw_id",
            foreignKey = @ForeignKey(name = "fk_quarantine_raw"), insertable = false, updatable = false)
    private Hl7RawMessage rawRef;

    @PrePersist
    void pp() {
        if (qId == null) qId = UUID.randomUUID().toString();
        if (quarantinedAt == null) quarantinedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
