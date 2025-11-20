/*
 * @ {#} BloodSample.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.entities;

import fit.instrument_service.enums.SampleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/*
 * @description: Thực thể đại diện cho mẫu máu trong hệ thống
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bloodSamples")
public class BloodSample extends BaseDocument {

    @Id
    private String id;

    @Field("barcode")
    @Indexed
    private String barcode; // Mã vạch của mẫu máu

    @Field("test_order_id")
    @Indexed
    private String testOrderId; // Mã đơn xét nghiệm liên quan đến mẫu máu

    @Field("workflow_id")
    @Indexed
    private String workflowId; // Mã quy trình xử lý mẫu

    @Field("instrument_id")
    @Indexed
    private String instrumentId; // Mã thiết bị phân tích mẫu

    @Field("cassette_id")
    private String cassetteId; // Mã cassette chứa mẫu

    @Field("status")
    private SampleStatus status; // Trạng thái hiện tại của mẫu máu

    @Field("is_test_order_auto_created")
    private boolean testOrderAutoCreated = false; // Đánh dấu nếu đơn xét nghiệm được tạo tự động

    @Field("skip_reason")
    private String skipReason; // Lý do bỏ qua mẫu (nếu có)

    @Field("notification_sent")
    private boolean notificationSent = false; // Đánh dấu nếu đã gửi thông báo về mẫu
}
