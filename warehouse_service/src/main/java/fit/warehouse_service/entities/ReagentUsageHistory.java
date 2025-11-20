/*
 * @ (#) ReagentUsageHistory.java    1.0    27/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 27/10/2025
 * @version: 1.0
 */

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
@Table(name = "reagent_usage_history")
@EntityListeners(AuditingEntityListener.class) // Bật Auditing chỉ cho lớp này
public class ReagentUsageHistory {

    @Id
    @Column(length = 64, updatable = false, nullable = false)
    private String id;

    /**
     * Thời điểm sử dụng (chỉ được tạo, không cập nhật)
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // Tương đương 'timestamp'

    /**
     * Người sử dụng (chỉ được tạo, không cập nhật)
     */
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdByUserId; // Tương đương 'userId'

    @ManyToOne(optional = false)
    @JoinColumn(name = "reagent_lot_id", updatable = false)
    private ReagentLot reagentLot;

    @ManyToOne
    @JoinColumn(name = "instrument_id", updatable = false)
    private Instrument instrument;

    @Column(updatable = false)
    private double quantityUsed;

    @Column(updatable = false)
    private String action; // e.g., "USED", "DISPOSED"

    /**
     * Tự động sinh ID trước khi lưu
     */
    @PrePersist
    public void autoGenerateId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = IdGenerator.generate("RUH");
        }
    }
}
