/*
 * @ {#} TestParameterResponse.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: DTO for ParameterRange response
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Builder
@Data
public class TestParameterResponse {
    private String testParameterId;
    private String paramName;
    private String abbreviation;
    private String description;
    private LocalDateTime createdAt;
    private String createdByUserId;
    private LocalDateTime updatedAt;
    private String updatedByUserId;
    private LocalDateTime deletedAt;
    private Boolean isDeleted;
    private List<ParameterRangeResponse> parameterRanges;
}
