/*
 * @ (#) ConfigurationSetting.java    1.0    27/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 27/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.enums.DataType;
import fit.warehouse_service.utils.IdGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "configuration_settings")
public class ConfigurationSetting extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataType dataType;

    @Column(length = 2048)
    private String value;

    @Override
    public String generateId() {
        return IdGenerator.generate("CFS"); // Tiền tố "CFS" (Config Setting)
    }
}
