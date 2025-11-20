/*
 * @ (#) RoleRepository.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import fit.iam_service.entities.Role;
import fit.iam_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {
    boolean existsByRoleCode(String roleCode);

    Optional<Role> findByRoleCode(String roleCode);

    Optional<Role> findByRoleIdAndIsDeletedFalse(String roleId);

    boolean existsByRoleNameAndIsDeletedFalse(String roleName);

    Optional<Role> findByRoleNameAndIsDeletedFalse(String roleName);

    boolean existsByRoleNameIgnoreCaseAndIsDeletedFalse(String roleName);

    boolean existsByRoleCodeIgnoreCaseAndIsDeletedFalse(String roleCode);

}
