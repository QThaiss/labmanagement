/*
 * @ (#) TestResultPublishedEvent.java    1.0    24/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.event;/*
 * @description:
 * @author: Bao Thong
 * @date: 24/11/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultPublishedEvent {
    private String instrumentId;
    private String testOrderId;
    private String barcode;
    private String hl7Message; // Đây là phần quan trọng nhất để xử lý
    private Map<String, String> rawResultData;
    private LocalDateTime publishedAt;
}
