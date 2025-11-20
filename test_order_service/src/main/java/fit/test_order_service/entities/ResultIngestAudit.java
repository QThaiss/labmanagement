/*
 * @ (#) ResultIngestAudit.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.IngestStatus;
import fit.test_order_service.utils.ResultIngestGenerator;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "result_ingest_audit", indexes = {
        @Index(name = "idx_ingest_msg", columnList = "message_id"),
        @Index(name = "idx_ingest_raw", columnList = "raw_id"),
        @Index(name = "idx_ingest_status", columnList = "status, updated_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultIngestAudit {
    @Id
    @Column(name = "ingest_id", length = 36, nullable = false, updatable = false)
    private String ingestId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "message_id", length = 64, nullable = false)
    private String messageId;

    @Column(name = "raw_id", length = 36)
    private String rawId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private IngestStatus status;

    @Size(max = 512)
    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "datetime(6)")
    private LocalDateTime updatedAt;

    /* Relation */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_id", referencedColumnName = "raw_id",
            foreignKey = @ForeignKey(name = "fk_ingest_raw"), insertable = false, updatable = false)
    private Hl7RawMessage rawRef;

    @PrePersist
    void pp() {
        if (ingestId == null) ingestId = ResultIngestGenerator.generateResultIngestId();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
        if (retryCount == null) retryCount = 0;
    }

    @PreUpdate
    void pu() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}

