/*
 * @ (#) BaseEntity.java    1.0    27/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 27/10/2025
 * @version: 1.0
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @Column(length = 64, updatable = false, nullable = false)
    private String id;

    // --- Creation Fields ---
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(updatable = false) // Mặc định là nullable
    private String createdByUserId;

    // --- Update Fields ---
    /**
     * Sẽ là NULL khi tạo mới (theo logic @PrePersist).
     * Sẽ được điền khi entity được cập nhật.
     */
    @LastModifiedDate
    @Column(nullable = true) // Cho phép null
    private LocalDateTime updatedAt;

    /**
     * Sẽ là NULL khi tạo mới (theo logic @PrePersist).
     * Sẽ được điền khi entity được cập nhật.
     */
    @LastModifiedBy
    @Column(nullable = true)
    private String updatedByUserId;

    // --- Soft Delete Fields ---
    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime deletedAt;

    /**
     * Phương thức trừu tượng, buộc các entity con phải
     * cung cấp logic sinh ID của riêng mình.
     */
    public abstract String generateId();

    /**
     * Lifecycle callback này chạy KHI TẠO MỚI.
     * Nó chạy SAU AuditingEntityListener, cho phép chúng ta
     * ghi đè giá trị của listener.
     */
    @PrePersist
    public void prePersistCallback() {
        // 1. Generate ID
        if (this.id == null || this.id.isEmpty()) {
            this.id = generateId();
        }

        // 2. Đặt trường update thành null khi tạo.
        // Mặc dù @LastModifiedDate đã điền chúng, @PrePersist
        // chạy sau và ghi đè chúng thành null.
        this.updatedAt = null;
        this.updatedByUserId = null;
    }
}