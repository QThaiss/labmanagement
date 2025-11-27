/*
 * @ (#) SystemEvent.java    1.0    26/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.events;/*
 * @description:
 * @author: Bao Thong
 * @date: 26/11/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemEvent {
    private String eventCode;      // VD: E_00001
    private String action;         // VD: Create Test Order
    private String message;        // Nội dung thông báo
    private String sourceService;  // VD: TEST_ORDER_SERVICE
    private String operator;       // Người thực hiện
    private Map<String, Object> details; // Dữ liệu chi tiết
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
}
