/*
 * @ (#) PrintJobResponse.java    1.0    22/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 22/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.JobStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PrintJobResponse {
    private String jobId;       // ID của job in
    private String orderId;     // ID của TestOrder được yêu cầu in
    private JobStatus status;   // Trạng thái hiện tại của job (vd: QUEUED)
    private String message;     // Thông báo cho người dùng
    private LocalDateTime requestedAt; // Thời điểm yêu cầu
}
