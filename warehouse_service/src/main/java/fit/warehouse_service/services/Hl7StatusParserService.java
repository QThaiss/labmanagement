/*
 * @ {#} Hl7StatusParser.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services;

import fit.warehouse_service.enums.InstrumentStatus;

/*
 * @description: Service interface for parsing HL7 messages to extract instrument status.
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
public interface Hl7StatusParserService {
    /**
     * Parses the HL7 message to extract the instrument status.
     *
     * @param hl7Message The HL7 message as a string.
     * @return The extracted InstrumentStatus.
     */
    InstrumentStatus parseStatusFromHl7(String hl7Message);
}
