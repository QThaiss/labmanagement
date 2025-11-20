/*
 * @ (#) FlaggingConfigVersion.java    1.0    11/10/2025
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
@Table(name = "flagging_config_version", indexes = {
        @Index(name = "idx_flagcfg_source_ver", columnList = "source, version", unique = true),
        @Index(name = "idx_flagcfg_activated", columnList = "activated_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlaggingConfigVersion {
    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @NotNull
    @Column(name = "version", nullable = false)
    private Integer version;

    @NotBlank
    @Size(max = 64)
    @Column(name = "source", length = 64, nullable = false)
    private String source;

    @Column(name = "activated_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime activatedAt;

    @Size(max = 64)
    @Column(name = "config_hash", length = 64)
    private String configHash;

    @Size(max = 255)
    @Column(name = "notes", length = 255)
    private String notes;

    @PrePersist
    void pp() {
        if (id == null) id = UUID.randomUUID().toString();
        if (activatedAt == null) activatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
