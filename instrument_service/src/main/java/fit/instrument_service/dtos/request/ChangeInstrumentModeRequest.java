/*
 * @ {#} ChangeInstrumentModeRequest.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description: Request DTO for changing the mode of an Instrument.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeInstrumentModeRequest {
    @Pattern(regexp = "READY|MAINTENANCE|INACTIVE",
            message = "newMode must be one of: READY, MAINTENANCE, INACTIVE")
    @NotNull(message = "newMode is required")
    private String newMode;

    private String reason;
}
