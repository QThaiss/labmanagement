/*
 * @ (#) Hl7RawMessage.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.utils.RawMessageGenerator;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "hl7_raw_message", indexes = {
        @Index(name = "uq_hl7_msg_id", columnList = "message_id", unique = true),
        @Index(name = "idx_hl7_src_time", columnList = "source, received_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hl7RawMessage {
    @Id
    @Column(name = "raw_id", length = 36, nullable = false, updatable = false)
    private String rawId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "message_id", length = 64, nullable = false, unique = true)
    private String messageId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "source", length = 64, nullable = false)
    private String source;

    @Lob
    @Column(name = "payload", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] payload;

    @Column(name = "received_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime receivedAt;

    @OneToMany(mappedBy = "rawRef", fetch = FetchType.LAZY)
    private List<ResultIngestAudit> ingestAudits;

    @OneToMany(mappedBy = "rawRef", fetch = FetchType.LAZY)
    private List<Hl7Quarantine> quarantines;

    @PrePersist
    void pp() {
        if (rawId == null) rawId = RawMessageGenerator.generateRawMessageId();
        if (receivedAt == null) receivedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}


