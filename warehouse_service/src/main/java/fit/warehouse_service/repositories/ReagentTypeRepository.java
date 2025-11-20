/*
 * @ (#) ReagentTypeRepository.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.entities.ReagentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReagentTypeRepository extends JpaRepository<ReagentType, String> {
    Optional<ReagentType> findByNameIgnoreCase(String name);
}
