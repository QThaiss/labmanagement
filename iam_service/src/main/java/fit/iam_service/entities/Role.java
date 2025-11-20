/*
 * @ (#) Role.java    1.0    30/09/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 30/09/2025
 * @version: 1.0
 */

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_roles_code", columnNames = "role_code"),
                @UniqueConstraint(name = "uq_roles_name", columnNames = "role_name")
        },
        indexes = {
                @Index(name = "idx_roles_name", columnList = "role_name"),
                @Index(name = "idx_roles_code", columnList = "role_code")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE roles SET is_deleted = true, deleted_at = NOW() WHERE role_id = ?")
@Where(clause = "is_deleted = false")
public class Role {
    @Id
    @Column(name = "role_id", length = 36, nullable = false, updatable = false)
    private String roleId;

    @Column(name = "role_code", length = 64, nullable = false)
    private String roleCode;   // ex: ADMIN, LAB_MANAGER

    @Column(name = "role_name", length = 128, nullable = false)
    private String roleName;

    @Column(name = "role_description", length = 255)
    private String roleDescription;

    @Builder.Default
    @Column(name = "is_system", nullable = false)
    private boolean isSystem = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime deletedAt;

    @Column(length = 36)
    private String deletedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 36)
    private String createdBy;

    private LocalDateTime updatedAt;

    @Column(length = 36)
    private String updatedBy;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RolePrivilege> rolePrivileges = new LinkedHashSet<>();

    @PrePersist
    void pp() {
        if (roleId == null) roleId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (roleCode != null) roleCode = roleCode.trim().toUpperCase(Locale.ROOT);
    }

    @PreUpdate
    void pu() {
        updatedAt = LocalDateTime.now();
    }

    public void replacePrivileges(Collection<Privilege> newPrivs) {
        Set<String> keep = newPrivs.stream().map(Privilege::getPrivilegeId).collect(Collectors.toSet());
        rolePrivileges.removeIf(rp -> !keep.contains(rp.getPrivilege().getPrivilegeId()));
        Set<String> have = rolePrivileges.stream().map(rp -> rp.getPrivilege().getPrivilegeId()).collect(Collectors.toSet());
        for (Privilege p : newPrivs)
            if (!have.contains(p.getPrivilegeId())) rolePrivileges.add(RolePrivilege.of(this, p));
    }
}
