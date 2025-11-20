/*
 * @ (#) InstrumentMapper.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.mappers;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.dtos.response.InstrumentResponse;
import fit.warehouse_service.entities.ConfigurationSetting;
import fit.warehouse_service.entities.Instrument;
import fit.warehouse_service.entities.ReagentType;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class InstrumentMapper {

    public InstrumentResponse toResponse(Instrument instrument) {
        return InstrumentResponse.builder()
                .id(instrument.getId())
                .name(instrument.getName())
                .status(instrument.getStatus())
                .isActive(instrument.isActive())
                .createdAt(instrument.getCreatedAt())
                .createdByUserId(instrument.getCreatedByUserId())
                .ipAddress(instrument.getIpAddress())
                .port(instrument.getPort())
                .protocolType(instrument.getProtocolType())
                .compatibleReagentIds(
                        instrument.getCompatibleReagents() != null ?
                                instrument.getCompatibleReagents().stream()
                                        .map(ReagentType::getId)
                                        .collect(Collectors.toSet()) : null // Handle null collections gracefully
                )
                .configurationSettingIds( // Renamed for clarity to match DTO field name
                        instrument.getConfigurations() != null ?
                                instrument.getConfigurations().stream()
                                        .map(ConfigurationSetting::getId)
                                        .collect(Collectors.toSet()) : null // Handle null collections gracefully
                )
                .updatedAt(instrument.getUpdatedAt())
                .updatedByUserId(instrument.getUpdatedByUserId())
                .deletedAt(instrument.getDeletedAt())
                .isDeleted(instrument.isDeleted())
                .build();
    }
}
