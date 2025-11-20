/*
 * @ {#} InstrumentStatusResponse.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.response;

import fit.warehouse_service.enums.InstrumentStatus;
import lombok.*;

import java.time.LocalDateTime;

/*
 * @description: Response DTO for instrument status check.
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstrumentStatusResponse {
    private String instrumentId;
    private String instrumentName;
    private InstrumentStatus currentStatus;
    private InstrumentStatus previousStatus;
    private boolean isActive;
    private boolean recheckPerformed;
    private LocalDateTime lastCheckedAt;
    private String statusMessage;
    private String errorDetails;
    private boolean canBeUsed;
}
