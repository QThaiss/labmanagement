/*
 * @ {#} CreateParameterRangeRequest.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.request;

/*
 * @description: DTO for creating ParameterRange
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateParameterRangeRequest {
    @NotBlank(message = "Abbreviation is required")
    private String abbreviation;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|BOTH)$", message = "Gender must be Male, Female, or Both")
    private String gender;

    @NotNull(message = "Minimum value is required")
    private Double minValue;

    @NotNull(message = "Maximum value is required")
    private Double maxValue;

    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;
}
