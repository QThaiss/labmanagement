/*
 * @ {#} PasswordResetRequestRepository.java   1.0     05/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.repositories;

import fit.iam_service.entities.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/*
 * @description: Repository interface for managing password reset requests
 * @author: Tran Hien Vinh
 * @date:   05/10/2025
 * @version:    1.0
 */
public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, String> {

    // Find an active password reset request by its token hash
    @Query("""
        select r from PasswordResetRequest r
        join fetch r.user u
        where r.tokenHash = :hash
          and r.expiresAt > :now
          and r.usedAt is null
    """)
    Optional<PasswordResetRequest> findActiveByHash(@Param("hash") String hash, @Param("now") LocalDateTime now);
}
