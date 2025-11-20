/*
 * @ (#) CreateConfigurationRequest.java    1.0    03/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 03/11/2025
 * @version: 1.0
 */

import fit.warehouse_service.enums.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO cho yêu cầu tạo mới một Configuration Setting.
 */
@Getter
@Setter
public class CreateConfigurationRequest {

    @NotBlank(message = "Configuration name cannot be blank.")
    @Size(max = 255, message = "Name must not exceed 255 characters.")
    @Pattern(
            regexp = "^[A-Z0-9]+(_[A-Z0-9]+)*$",
            message = "Configuration name must be in uppercase, separated by underscores (e.g. CONFIG_NAME)."
    )
    private String name;

    private String description;

    @NotNull(message = "Data type cannot be null.")
    private DataType dataType;

    @Size(max = 2048, message = "Value must not exceed 2048 characters.")
    private String value;
}
