/*
 * @ {#} FilterInfo.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import fit.test_order_service.enums.Gender;
import fit.test_order_service.enums.OrderStatus;
import fit.test_order_service.enums.ReviewMode;
import fit.test_order_service.enums.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/*
 * @description: Filter information for querying orders
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterInfo {
    private String search;
    private LocalDate startDate;
    private LocalDate endDate;
    private OrderStatus status;
    private ReviewStatus reviewStatus;
    private Gender gender;
    private ReviewMode reviewMode;
    private String createdBy;
    private String reviewedBy;
}
