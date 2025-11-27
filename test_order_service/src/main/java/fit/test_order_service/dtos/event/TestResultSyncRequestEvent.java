/*
 * @ (#) TestResultSyncRequestEvent.java    1.0    25/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.event;/*
 * @description:
 * @author: Bao Thong
 * @date: 25/11/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultSyncRequestEvent {
    private String requestId;
    private List<String> barcodes; // Danh sách barcode cần đồng bộ lại
    private String requestedBy;    // Service nào yêu cầu (ví dụ: "TEST_ORDER_SERVICE")
}
