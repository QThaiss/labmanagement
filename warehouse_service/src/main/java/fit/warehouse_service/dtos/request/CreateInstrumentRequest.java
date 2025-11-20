/*
 * @ (#) CreateInstrumentRequest.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.enums.ProtocolType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CreateInstrumentRequest {
    @NotEmpty(message = "Instrument name cannot be empty.")
    private String name;

    @NotEmpty(message = "IP address cannot be empty.")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "Invalid IP address format. Example: 192.168.1.1"
    )
    private String ipAddress;

    @NotNull(message = "Port cannot be null.")
    @Positive(message = "Port must be a positive number.")
    private Integer port;

    private ProtocolType protocolType;

    private Set<@Pattern(
            regexp = "^RTY-[A-Za-z0-9]+(-[A-Za-z0-9]+)+$",
            message = "Compatible Reagent ID must start with 'RTY-' and components separated by hyphens. Example: RTY-ALINITY-002"
    ) String> compatibleReagentIds;

    private Set<@Pattern(
            regexp = "^CFS-[A-Za-z0-9]+(-[A-Za-z0-9]+)+$",
            message = "Configuration Setting ID must start with 'CFS-' and components separated by hyphens. Example: CFS-251103111530-de796b1a-7950"
    ) String> configurationSettingIds;

    private String cloneFromInstrumentId;

    @NotEmpty(message = "Model cannot be empty.")
    private String model;

    @NotEmpty(message = "Type cannot be empty.")
    private String type;

    @NotEmpty(message = "Serial number cannot be empty.")
    @Pattern(
            regexp = "^[A-Za-z0-9]+(-[A-Za-z0-9]+)+$",
            message = "Serial number must consist of alphanumeric components separated by hyphens. Example: C8K-SN-TEST-001"
    )
    private String serialNumber;

    @NotEmpty(message = "Vendor ID cannot be empty.")
    @Pattern(
            regexp = "^VDR-[A-Za-z0-9]+(-[A-Za-z0-9]+)+$",
            message = "Vendor ID must start with 'VDR-' and components separated by hyphens. Example: VDR-251104142101-b2c3d4e5-1002"
    )
    private String vendorId;
}