/*
 * @ (#) RawTestResult.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import fit.instrument_service.enums.PublishStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rawTestResults")
// Lưu trữ kết quả thô tạm thời trên service trước khi bị xóa
public class RawTestResult extends BaseDocument {

    @Id
    private String id;

    @Field("instrument_id")
    private String instrumentId; // 'id' của Instrument

    @Field("test_order_id")
    private String testOrderId; // ID của Test Order (Req 3.6.1.2)

    @Field("barcode")
    private String barcode; // Barcode của mẫu (Req 3.6.1.2)

    @Field("raw_result_data")
    private Object rawResultData; // Dữ liệu thô (dùng Object hoặc Map cho linh hoạt)

    @Field("hl7_message")
    private String hl7Message; // Chuỗi message HL7 đã chuyển đổi (Req 3.6.1.3)

    @Field("publish_status")
    private PublishStatus publishStatus; // Trạng thái: "Pending", "Sent", "Failed" (Req 3.6.1.3)

    @Field("is_ready_for_deletion")
    private boolean isReadyForDeletion = false; // Cờ báo đã sync với Monitoring Service (Req 3.6.1.5)
}
