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

    @Column(length = 1000)
    private String description;

    // --- Các trường mới thêm vào để Sync với Instrument Service ---

    @Column(name = "config_type", nullable = false)
    private String configType; // "General" hoặc "Specific"

    @Column(name = "instrument_model")
    private String instrumentModel;

    @Column(name = "instrument_type")
    private String instrumentType;

    @Column(nullable = false)
    private String version;

    // Lưu Map<String, Object> settings dưới dạng chuỗi JSON
    @Lob // Sử dụng Large Object để lưu JSON dài
    @Column(name = "settings", columnDefinition = "TEXT")
    private String settings;

    @Override
    public String generateId() {
        return IdGenerator.generate("CFS"); // Config Setting
    }
}
