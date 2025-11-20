/*
 * @ (#) ReagentType.java    1.0    27/10/2025
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

@Getter
@Setter
@Entity
@Table(name = "reagent_types")
public class ReagentType extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String catalogNumber;

    private String manufacturer;

    private String casNumber;

    @Column(length = 1024)
    private String description;

    @Column(name = "usage_per_run")
    private String usagePerRun;

    @Override
    public String generateId() {
        return IdGenerator.generate("RTY"); // Tiền tố "RTY" (Reagent Type)
    }
}
