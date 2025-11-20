/*
 * @ (#) Privilege.java    1.0    30/09/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 30/09/2025
 * @version: 1.0
 */

import fit.iam_service.enums.PrivilegeCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "privileges",
        uniqueConstraints = @UniqueConstraint(name = "uq_privileges_code", columnNames = "privilege_code"),
        indexes = @Index(name = "idx_privileges_name", columnList = "privilege_name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Privilege {
    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String privilegeId;

    @Enumerated(EnumType.STRING)
    @Column(length = 100, nullable = false)
    private PrivilegeCode privilegeCode;

    @Column(length = 150, nullable = false)
    private String privilegeName;

    @Column(length = 255)
    private String privilegeDescription;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 36)
    private String createdBy;

    private LocalDateTime updatedAt;

    @Column(length = 36)
    private String updatedBy;

    @PrePersist
    void pp() {
        if (privilegeId == null) privilegeId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    void pu() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
