/*
 * @ {#} CheckInstrumentStatusRequest.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/*
 * @description: Request DTO for checking the status of an instrument.
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
@Getter
@Setter
public class CheckInstrumentStatusRequest {
    @NotBlank(message = "Instrument ID is required")
    private String instrumentId;

    // Lựa chọn để ép buộc kiểm tra lại trạng thái của thiết bị
    private boolean forceRecheck = false;
}
