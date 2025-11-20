/*
 * @ {#} ModifyConfigurationRequest.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.request;

import fit.warehouse_service.enums.DataType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description: DTO for modifying an existing Configuration Setting.
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyConfigurationRequest {
    @NotNull(message = "New value is required")
    private Object newValue;

    @Size(max = 500, message = "Modification reason cannot exceed 500 characters")
    private String modificationReason;
}
