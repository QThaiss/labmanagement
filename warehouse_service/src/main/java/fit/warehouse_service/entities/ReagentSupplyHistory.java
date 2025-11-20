/*
 * @ (#) ReagentSupplyHistory.java    1.0    27/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 27/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.enums.SupplyStatus;
import fit.warehouse_service.utils.IdGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reagent_supply_history")
public class ReagentSupplyHistory extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "reagent_type_id") // Khóa ngoại String
    private ReagentType reagentType;

    @ManyToOne
    @JoinColumn(name = "vendor_id") // Tên cột khoá ngoại
    private Vendor vendor;
    private String poNumber;
    private LocalDate orderDate;
    private LocalDateTime receiptDate;
    private double quantityReceived;
    private String unitOfMeasure;
    private String lotNumber;
    private LocalDate expirationDate;
    private String receivedByUserId; // Ghi lại id, nhưng dùng createdByUserId cho "ai tạo"
    private String initialStorageLocation;

    @Enumerated(EnumType.STRING)
    private SupplyStatus status;

    @Override
    public String generateId() {
        return IdGenerator.generate("RSH"); // Tiền tố "RSH"
    }
}