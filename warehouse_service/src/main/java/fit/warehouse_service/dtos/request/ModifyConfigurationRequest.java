/*
 * @ {#} ModifyConfigurationRequest.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ModifyConfigurationRequest {

    @NotNull(message = "Settings cannot be null.")
    private Map<String, Object> settings; // Thay thế cho newValue cũ

    @Size(max = 255, message = "Reason must not exceed 255 characters.")
    private String modificationReason;

    // Có thể cho phép cập nhật version khi sửa đổi
    private String version;
}
