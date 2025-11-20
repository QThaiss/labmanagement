/*
 * @ (#) ReportJobRepository.java    1.0    22/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 22/10/2025
 * @version: 1.0
 */

import fit.test_order_service.entities.ReportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportJobRepository extends JpaRepository<ReportJob, String> {
}
