/*
 * @ (#) RawTestResultBackup.java    1.0    19/11/2025
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
@Document(collection = "raw_test_result_backups")
public class RawTestResultBackup {

    @Id
    private String id;

    @Field("raw_hl7_message")
    private String rawHl7Message; // Nội dung tin nhắn HL7 thô [cite: 914]

    @Field("test_order_id")
    private String testOrderId; // ID của Test Order (nếu parse được) để dễ truy vết [cite: 477]

    @Field("instrument_id")
    private String instrumentId; // ID thiết bị gửi kết quả

    @Field("captured_at")
    private LocalDateTime capturedAt; // Thời điểm Monitoring Service nhận được tin nhắn [cite: 461]

    @Field("sync_status")
    private String syncStatus; // Trạng thái đồng bộ (VD: "SYNCED", "PENDING") để hỗ trợ flow Sync-up [cite: 473]
}
