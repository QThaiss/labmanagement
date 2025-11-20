/*
 * @ {#} UpdateTestParameterRequest.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/*
 * @description: DTO for updating TestParameter
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Getter
@Setter
public class UpdateTestParameterRequest {
    @Size(max = 100, message = "Parameter name must not exceed 100 characters")
    private String paramName;

    @Size(max = 20, message = "Abbreviation must not exceed 20 characters")
    private String abbreviation;

    private String description;
}
