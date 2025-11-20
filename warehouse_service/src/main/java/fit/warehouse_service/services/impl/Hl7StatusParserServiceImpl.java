/*
 * @ {#} Hl7StatusParserServiceImpl.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.parser.Parser;
import fit.warehouse_service.enums.InstrumentStatus;
import fit.warehouse_service.services.Hl7StatusParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
 * @description: Implementation of Hl7StatusParserService to parse instrument status from HL7 messages.
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Hl7StatusParserServiceImpl implements Hl7StatusParserService {
    private final Parser parser;

    @Override
    public InstrumentStatus parseStatusFromHl7(String hl7Message) {
        try {
            Message message = parser.parse(hl7Message);
            if (message instanceof ORU_R01 oru) {
                log.info("Parsing HL7 ORU_R01 message for instrument status.");
                int orderObsCount = oru.getPATIENT_RESULT().getORDER_OBSERVATIONReps();
                for (int i = 0; i < orderObsCount; i++) {
                    ORU_R01_ORDER_OBSERVATION obs = oru.getPATIENT_RESULT().getORDER_OBSERVATION(i);
                    int obxCount = obs.getOBSERVATIONReps();
                    for (int j = 0; j < obxCount; j++) {
                        OBX obx = obs.getOBSERVATION(j).getOBX();
                        String id = obx.getObservationIdentifier().getIdentifier().getValue();
                        String value = obx.getObservationValue(0).getData().toString();

                        log.info("Found OBX segment - ID: {}, Value: {}", id, value);

                        if ("STATUS".equalsIgnoreCase(id)) {
                            if (value.equalsIgnoreCase("READY")) return InstrumentStatus.READY;
                            if (value.equalsIgnoreCase("PROCESSING")) return InstrumentStatus.PROCESSING;
                            if (value.equalsIgnoreCase("MAINTENANCE")) return InstrumentStatus.MAINTENANCE;
                            if (value.equalsIgnoreCase("ERROR")) return InstrumentStatus.ERROR;
                        }
                    }
                }
            }
        } catch (HL7Exception e) {
            log.error("Failed to parse HL7 status message: {}", e.getMessage());
        }
        // Fallback: nếu không có STATUS, giả định là ERROR
        log.warn("No STATUS segment found in HL7 message. Defaulting to READY.");
        return InstrumentStatus.ERROR;
    }
}
