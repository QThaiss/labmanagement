/*
 * @ (#) PasswordHistoryRepository.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import fit.iam_service.entities.PasswordHistory;
import fit.iam_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, String> {
    List<PasswordHistory> findTop5ByUserOrderByChangedAtDesc(User user);
}
