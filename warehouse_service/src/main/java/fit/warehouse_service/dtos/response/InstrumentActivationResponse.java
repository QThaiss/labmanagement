/*
 * @ {#} InstrumentActivationResponse.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.response;

import fit.warehouse_service.enums.InstrumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/*
 * @description: Response DTO for instrument activation/deactivation actions.
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
@Data
@Builder
public class InstrumentActivationResponse {
    private String instrumentId;
    private String instrumentName;
    private boolean isActive;
    private InstrumentStatus currentStatus;
    private String actionPerformed;
    private String reason;
    private LocalDateTime actionTimestamp;
    private String message;
    private boolean canBeUsedForTestOrders;
}
