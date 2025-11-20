/*
 * @ {#} ParameterRange.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.entities;

import fit.warehouse_service.utils.IdGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/*
 * @description: Entity representing reference ranges for test parameters based
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Getter
@Setter
@Entity
@Table(name = "parameter_ranges")
public class ParameterRange extends BaseEntity{
    @Column(name = "gender", length = 10)
    String gender; // Male, Female, hoặc Both

    @Column(name = "min_value")
    Double minValue;

    @Column(name = "max_value")
    Double maxValue;

    @Column(name = "unit", length = 20)
    String unit; // g/dL, %, cells/μL...

    @ManyToOne
    @JoinColumn(name = "test_parameter_id")
    TestParameter testParameter;

    @Override
    public String generateId() {
        return IdGenerator.generate("PR");
    }
}
