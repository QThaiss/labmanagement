/*
 * @ (#) ConfigurationCreatedEvent.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.events;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationCreatedEvent implements Serializable {
    private String id;
    private String name;
    private String dataType; // Sử dụng String để đơn giản hóa việc truyền enum
    private String value;
    private String description;
}
