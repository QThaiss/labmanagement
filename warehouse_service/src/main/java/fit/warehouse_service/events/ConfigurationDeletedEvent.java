/*
 * @ (#) ConfigurationDeletedEvent.java    1.0    12/11/2025
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
public class ConfigurationDeletedEvent implements Serializable {
    private String configurationId;
}
