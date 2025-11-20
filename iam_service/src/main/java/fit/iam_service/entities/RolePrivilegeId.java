/*
 * @ (#) RolePrivilegeId.java    1.0    30/09/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 30/09/2025
 * @version: 1.0
 */

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RolePrivilegeId implements Serializable {
    @Column(name = "role_id", length = 36)
    private String roleId;

    @Column(name = "privilege_id", length = 36)
    private String privilegeId;
}
