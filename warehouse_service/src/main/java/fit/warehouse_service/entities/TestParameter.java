/*
 * @ {#} TestParameter.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.entities;

import fit.warehouse_service.utils.IdGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * @description: Entity representing reference ranges for test parameters based
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Getter
@Setter
@Entity
@Table(name = "test_parameters")
public class TestParameter extends BaseEntity{
    @Column(name = "param_name", nullable = false, length = 100)
    String paramName; // e.g. Hemoglobin

    @Column(name = "abbreviation", length = 20)
    String abbreviation; // e.g. Hb/HGB

    @Column(name = "description", columnDefinition = "TEXT")
    String description; // e.g. Measures the amount of hemoglobin in the blood


    @OneToMany(mappedBy = "testParameter", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ParameterRange> parameterRanges;

    @Override
    public String generateId() {
        return IdGenerator.generate("TP");
    }
}
