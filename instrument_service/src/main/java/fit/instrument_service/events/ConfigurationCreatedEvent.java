/*
 * @ (#) ConfigurationCreatedEvent.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.events;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationCreatedEvent implements Serializable {
    private String id;
    private String name;
    private String configType;      // Mới
    private String instrumentModel; // Mới
    private String instrumentType;  // Mới
    private String version;         // Mới
    private Map<String, Object> settings; // Thay thế cho value/dataType
    private String description;
}
