/*
 * @ {#} InstrumentMapper.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.mappers;

import fit.instrument_service.dtos.response.InstrumentReagentResponse;
import fit.instrument_service.dtos.response.InstrumentResponse;
import fit.instrument_service.entities.Instrument;
import fit.instrument_service.entities.InstrumentReagent;
import org.springframework.stereotype.Component;

/*
 * @description: Mapper class for converting Instrument entities to InstrumentResponse DTOs.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Component
public class InstrumentMapper {
    public static InstrumentResponse toResponse(Instrument instrument) {
        return InstrumentResponse.builder()
                .id(instrument.getId())
                .name(instrument.getName())
                .model(instrument.getModel())
                .mode(instrument.getMode())
                .status(instrument.getStatus())
                .lastModeChangeReason(instrument.getLastModeChangeReason())
                .updatedAt(instrument.getUpdatedAt())
                .updatedBy(instrument.getUpdatedBy())
                .build();
    }
    public static InstrumentReagentResponse toReagentResponse(InstrumentReagent reagent) {
        return InstrumentReagentResponse.builder()
                .id(reagent.getId())
                .instrumentId(reagent.getInstrumentId())
                .reagentName(reagent.getReagentName())
                .lotNumber(reagent.getLotNumber())
                .quantity(reagent.getQuantity())
                .expirationDate(reagent.getExpirationDate())
                .status(reagent.getStatus())
                .vendor(reagent.getVendor())
                .createdAt(reagent.getCreatedAt())
                .createdBy(reagent.getCreatedBy())
                .updatedAt(reagent.getUpdatedAt())
                .updatedBy(reagent.getUpdatedBy())
                .build();
    }
}
