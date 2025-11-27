/*
 * @ (#) EventLogFilterRequest.java    1.0    26/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 26/11/2025
 * @version: 1.0
 */

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class EventLogFilterRequest {
    private String keyword;       // Tìm chung trong action hoặc message
    private String action;        // Lọc chính xác theo Action
    private String sourceService; // Lọc theo Service nguồn (IAM, ORDER, etc.)
    private String operator;      // Lọc theo người thực hiện

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate; // Lọc từ ngày

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;   // Lọc đến ngày
}
