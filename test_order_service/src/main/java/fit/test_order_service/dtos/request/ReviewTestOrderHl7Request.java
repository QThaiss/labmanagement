/*
 * @ (#) ReviewTestOrderHl7Request.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.ReviewMode;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for reviewing a test order.
 * Adjustments are now handled via an optional HL7 message string.
 */
@Data
public class ReviewTestOrderHl7Request {

    // Mặc định là HUMAN nếu không cung cấp
    private ReviewMode reviewMode = ReviewMode.HUMAN;

    @Size(max = 500, message = "Overall review note cannot exceed 500 characters")
    private String note;

    /**
     * Optional HL7 message string for result adjustments.
     * If provided, this message will be processed by the Hl7ProcessingService.
     */
    @Size(max = 5000, message = "HL7 message cannot exceed 5000 characters")
    private String hl7Message;
}
