/*
 * @ {#} TestParameterResponse.java   1.0     20/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.client.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   20/11/2025
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
