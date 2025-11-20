/*
 * @ (#) RolePrivilege.java    1.0    30/09/2025
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

@Entity
@Table(name = "role_privileges", indexes = @Index(name = "idx_rp_privilege", columnList = "privilege_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePrivilege {
    @EmbeddedId
    private RolePrivilegeId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rp_role"))
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("privilegeId")
    @JoinColumn(name = "privilege_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rp_priv"))
    private Privilege privilege;

    public static RolePrivilege of(Role r, Privilege p) {
        return RolePrivilege.builder()
                .id(new RolePrivilegeId(r.getRoleId(), p.getPrivilegeId()))
                .role(r).privilege(p).build();
    }
}