/*
 * @ {#} InstrumentResponse.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.dtos.response;

import fit.instrument_service.enums.InstrumentMode;
import fit.instrument_service.enums.InstrumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/*
 * @description: Response DTO for Instrument details.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Data
@Builder
public class InstrumentResponse {
    private String id;

    private String name;

    private String model;

    private InstrumentMode mode;

    private InstrumentStatus status;

    private String lastModeChangeReason;

    private LocalDateTime updatedAt;

    private String updatedBy;
}
