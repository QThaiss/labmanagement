/*
 * @ {#} DeactivateInstrumentRequest.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/*
 * @description: Request DTO for deactivating an instrument in the warehouse system.
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
@Data
public class DeactivateInstrumentRequest {
    @NotBlank(message = "Instrument ID is required")
    private String instrumentId;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;
}
