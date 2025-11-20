/*
 * @ (#) InstrumentResponse.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.enums.InstrumentStatus;
import fit.warehouse_service.enums.ProtocolType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
public class InstrumentResponse {
    private String id;
    private String name;
    private InstrumentStatus status;
    private boolean isActive;
    private String ipAddress;
    private int port;
    private ProtocolType protocolType;
    private Set<String> compatibleReagentIds;
    private Set<String> configurationSettingIds;
    private LocalDateTime createdAt;
    private String createdByUserId;
    private LocalDateTime updatedAt;
    private String updatedByUserId;
    private LocalDateTime deletedAt;
    private Boolean isDeleted;
}
