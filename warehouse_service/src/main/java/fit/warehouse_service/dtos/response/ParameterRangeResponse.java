/*
 * @ {#} ParameterRangeResponse.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/*
 * @description: DTO for ParameterRange response
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Builder
@Data
public class ParameterRangeResponse {
    private String parameterRangeId;
    private String gender;
    private Double minValue;
    private Double maxValue;
    private String unit;
    private String testParameterId;
    private String createdByUserId;
    private LocalDateTime createdAt;
    private String updatedByUserId;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Boolean isDeleted;
}
