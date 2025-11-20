/*
 * @ (#) ReagentLot.java    1.0    27/10/2025
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

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "reagent_lots")
public class ReagentLot extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "reagent_type_id") // Khóa ngoại String
    private ReagentType reagentType;

    @Column(nullable = false)
    private String lotNumber;

    @Column(nullable = false)
    private LocalDate expirationDate;

    private double currentQuantity;
    private String unitOfMeasure;

    @OneToOne
    @JoinColumn(name = "supply_history_id") // Khóa ngoại String
    private ReagentSupplyHistory supplyRecord;

    @Override
    public String generateId() {
        return IdGenerator.generate("RLO"); // Tiền tố "RLO" (Reagent Lot)
    }
}