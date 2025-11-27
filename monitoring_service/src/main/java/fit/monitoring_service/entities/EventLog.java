/*
 * @ (#) EventLog.java    1.0    25/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 25/11/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "event_logs") // Lưu trong MongoDB của Monitoring Service
public class EventLog {

    @Id
    private String id;

    // Mã sự kiện (Ví dụ: E_00001, E_00002...) lấy từ bảng Event Table
    @Field("event_code")
    private String eventCode;

    // Loại hành động hoặc tên sự kiện (Ví dụ: "Create Test Order", "Login") [cite: 449]
    @Field("action")
    private String action;

    // Mô tả ngắn gọn hoặc thông báo sự kiện [cite: 450]
    @Field("message")
    private String message;

    // ID của Service gửi log (Ví dụ: "IAM_SERVICE", "TEST_ORDER_SERVICE")
    // Cần thiết để biết log đến từ đâu [cite: 1001]
    @Field("source_service")
    private String sourceService;

    // Người thực hiện hành động (User ID hoặc Username)
    @Field("operator")
    private String operator;

    // Chi tiết sự kiện (Lưu trữ JSON linh hoạt cho view detail)
    // Có thể chứa before/after data như trong OrderEventLog
    @Field("details")
    private Map<String, Object> details;

    // Thời điểm xảy ra sự kiện
    @Field("created_at")
    private LocalDateTime createdAt;

    // Các thông tin bổ sung về môi trường (tham khảo từ AuditLog của IAM)
    @Field("ip_address")
    private String ipAddress;

    @Field("user_agent")
    private String userAgent;
}
