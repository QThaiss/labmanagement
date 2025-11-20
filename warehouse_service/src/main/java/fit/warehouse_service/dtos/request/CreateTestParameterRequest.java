/*
 * @ {#} CreateTestParameterRequest.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/*
 * @description: DTO for creating a new Test Parameter
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Getter
@Setter
public class CreateTestParameterRequest {
    @NotBlank(message = "Parameter name is required")
    @Size(max = 100, message = "Parameter name must not exceed 100 characters")
    private String paramName;

    @Size(max = 20, message = "Abbreviation must not exceed 20 characters")
    private String abbreviation;

    private String description;
}
