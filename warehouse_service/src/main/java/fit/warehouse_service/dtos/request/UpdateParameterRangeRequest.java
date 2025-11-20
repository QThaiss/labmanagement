/*
 * @ {#} UpdateParameterRangeRequest.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.request;

import jakarta.validation.constraints.Pattern;;
import lombok.Getter;
import lombok.Setter;

/*
 * @description: DTO for updating ParameterRange
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Getter
@Setter
public class UpdateParameterRangeRequest {
    @Pattern(regexp = "^(MALE|FEMALE|BOTH)$", message = "Gender must be MALE, FEMALE, or BOTH")
    private String gender;

    private Double minValue;

    private Double maxValue;

    private String unit;
}
