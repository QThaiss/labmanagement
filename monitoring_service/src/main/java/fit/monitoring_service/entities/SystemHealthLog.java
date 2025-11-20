/*
 * @ (#) SystemHealthLog.java    1.0    19/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 19/11/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "system_health_logs")
public class SystemHealthLog {

    @Id
    private String id;

    @Field("component_name")
    private String componentName; // Ví dụ: "Message Broker", "Database"

    @Field("status")
    private String status; // "UP", "DOWN", "UNRESPONSIVE"

    @Field("error_code")
    private String errorCode; // Mã lỗi nếu có [cite: 486]

    @Field("retry_attempts")
    private Integer retryAttempts; // Số lần thử lại trước khi log lỗi [cite: 486]

    @Field("details")
    private String details; // Chi tiết lỗi hoặc thông báo phục hồi

    @Field("logged_at")
    private LocalDateTime loggedAt; // Timestamp [cite: 486]
}
