/*
 * @ (#) ReviewTestOrderResponse.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewTestOrderResponse {
    private String orderId;
    private ReviewStatus reviewStatus;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private int adjustmentsLogged;
    private String message;
}
