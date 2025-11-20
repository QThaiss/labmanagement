/*
 * @ {#} Hl7MessageRequest.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package fit.test_order_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description: DTO for HL7 message processing request
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hl7MessageRequest {
    @NotBlank(message = "HL7 payload is required")
    private String hl7Payload;
}
