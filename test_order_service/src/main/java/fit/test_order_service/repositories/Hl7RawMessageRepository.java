/*
 * @ {#} Hl7RawMessageRepository.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.repositories;

import fit.test_order_service.entities.Hl7RawMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * @description: Repository for managing Hl7Quarantine entities.
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
@Repository
public interface Hl7RawMessageRepository extends JpaRepository<Hl7RawMessage, String> {
    /**
     * Kiểm tra sự tồn tại của một Hl7RawMessage dựa trên messageId.
     *
     * @param messageId ID của tin nhắn HL7 cần kiểm tra.
     * @return true nếu tin nhắn tồn tại, false nếu không.
     */
    boolean existsByMessageId(String messageId);
}
