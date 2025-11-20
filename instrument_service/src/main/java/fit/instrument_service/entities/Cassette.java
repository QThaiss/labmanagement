/*
 * @ {#} Cassette.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/*
 * @description: Thực thể đại diện cho cassette trong hệ thống
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cassettes")
public class Cassette extends BaseDocument {

    @Id
    private String id;

    @Field("cassette_identifier")
    @Indexed(unique = true)
    private String cassetteIdentifier; // Mã định danh duy nhất của cassette

    @Field("instrument_id")
    @Indexed
    private String instrumentId; // Mã thiết bị phân tích mẫu liên quan đến cassette

    @Field("queue_position")
    private Integer queuePosition; // Vị trí trong hàng đợi xử lý

    @Field("is_processed")
    private boolean processed = false; // Đánh dấu nếu cassette đã được xử lý

    @Field("processed_at")
    private LocalDateTime processedAt; // Thời gian cassette được xử lý

    @Field("workflow_id")
    private String workflowId; // Mã quy trình xử lý liên quan đến cassette
}
