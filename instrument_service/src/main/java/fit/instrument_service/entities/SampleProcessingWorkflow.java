/*
 * @ {#} SampleProcessingWorkflow.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.entities;

import fit.instrument_service.enums.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Thực thể đại diện cho quy trình xử lý mẫu trong hệ thống
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sampleProcessingWorkflows")
public class SampleProcessingWorkflow extends BaseDocument {

    @Id
    private String id;

    @Field("instrument_id")
    @Indexed
    private String instrumentId; // Mã thiết bị phân tích mẫu liên quan đến quy trình

    @Field("cassette_id")
    private String cassetteId; // Mã cassette chứa mẫu liên quan đến quy trình

    @Field("status")
    private WorkflowStatus status; // Trạng thái hiện tại của quy trình xử lý mẫu

    @Field("sample_ids")
    private List<String> sampleIds; // Danh sách mã mẫu máu liên quan đến quy trình

    @Field("started_at")
    private LocalDateTime startedAt; // Thời gian bắt đầu quy trình

    @Field("completed_at")
    private LocalDateTime completedAt; // Thời gian hoàn thành quy trình

    @Field("reagent_check_passed")
    private boolean reagentCheckPassed = false; // Đánh dấu nếu kiểm tra hóa chất đã vượt qua

    @Field("test_order_service_available")
    private boolean testOrderServiceAvailable = true; // Đánh dấu nếu đơn xét nghiệm có sẵn

    @Field("error_message")
    private String errorMessage; // Thông điệp lỗi nếu có xảy ra

    @Field("results_converted_to_hl7")
    private boolean resultsConvertedToHl7 = false; // Đánh dấu nếu kết quả đã được chuyển đổi sang định dạng HL7

    @Field("results_published")
    private boolean resultsPublished = false; // Đánh dấu nếu kết quả đã được công bố
}
