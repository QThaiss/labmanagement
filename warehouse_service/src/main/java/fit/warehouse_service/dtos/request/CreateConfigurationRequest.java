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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

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

    @Size(max = 1000, message = "Description must not exceed 1000 characters.")
    private String description;

    // Các trường mới được thêm vào để khớp với Instrument Service (Req 3.6.3.1)

    @NotBlank(message = "Configuration type is required (GENERAL/SPECIFIC).")
    @Pattern(regexp = "^(GENERAL|SPECIFIC)$", message = "Configuration type must be either 'General' or 'Specific'.")
    private String configType; // Tương ứng với ConfigurationType bên Instrument Service

    private String instrumentModel; // Bắt buộc nếu configType là "Specific"

    private String instrumentType;  // Bắt buộc nếu configType là "Specific"

    @NotBlank(message = "Version is required.")
    private String version;

    @NotNull(message = "Settings cannot be null.")
    private Map<String, Object> settings;
}
