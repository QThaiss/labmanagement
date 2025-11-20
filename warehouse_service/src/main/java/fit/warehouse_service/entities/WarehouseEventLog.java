/*
 * @ (#) WarehouseEventLog.java    1.0    27/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 27/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.enums.WarehouseActionType;
import fit.warehouse_service.utils.IdGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "warehouse_event_logs")
@EntityListeners(AuditingEntityListener.class)
public class WarehouseEventLog {

    @Id
    @Column(length = 64, updatable = false, nullable = false)
    private String id;

    /**
     * Thời điểm log được tạo (chỉ được tạo, không cập nhật)
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // Tương đương 'timestamp'

    /**
     * Người thực hiện hành động (chỉ được tạo, không cập nhật)
     */
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdByUserId; // Tương đương 'userId'

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private WarehouseActionType action;

    @Column(updatable = false)
    private String entityType;

    @Column(length = 64, updatable = false)
    private String entityId;

    @Column(length = 2048, updatable = false)
    private String details;

    /**
     * Tự động sinh ID trước khi lưu
     */
    @PrePersist
    public void autoGenerateId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = IdGenerator.generate("LOG");
        }
    }
}
