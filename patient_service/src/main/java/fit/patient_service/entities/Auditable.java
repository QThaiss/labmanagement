/*
 * @ (#) Auditable.java    1.0    22/09/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.patient_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 22/09/2025
 * @version: 1.0
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class Auditable {
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;        // userId ở IAM

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    // Soft delete (tuân thủ retention)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
