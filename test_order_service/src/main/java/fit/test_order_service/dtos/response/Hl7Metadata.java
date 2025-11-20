/*
 * @ {#} Hl7Metadata.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description: DTO for HL7 message metadata
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hl7Metadata {
    private String messageId;         // MSH-10
    private String sendingApplication; // MSH-3
    private String sendingFacility;    // MSH-4
}
