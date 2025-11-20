/*
 * @ (#) EmailOtpRepository.java    1.0    06/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 06/10/2025
 * @version: 1.0
 */

import fit.iam_service.entities.EmailOtp;
import fit.iam_service.entities.User;
import fit.iam_service.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    // Bản ghi hoạt động (is_deleted = false)
    Optional<EmailOtp> findFirstByUserAndPurposeAndDeletedFalseOrderByCreatedAtDesc(User user, OtpPurpose purpose);
}