/*
 * @ (#) ConfigurationUpdatedEvent.java    1.0    25/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.events;/*
 * @description:
 * @author: Bao Thong
 * @date: 25/11/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationUpdatedEvent {
    private String id;
    private String name;
    private String version;
    private Map<String, Object> settings; // Các cài đặt mới
    private String modificationReason;
}