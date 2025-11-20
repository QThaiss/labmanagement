/*
 * @ (#) TestOrderItemResponse.java    1.0    16/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.client.dtos;/*
 * @description:
 * @author: Bao Thong
 * @date: 16/10/2025
 * @version: 1.0
 */

import fit.instrument_service.client.dtos.enums.ItemStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TestOrderItemResponse {
    private String id;
    private String testCode;
    private String testName;
    private String unit;
    private String referenceRange;
    private ItemStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private boolean isDeleted;
}
