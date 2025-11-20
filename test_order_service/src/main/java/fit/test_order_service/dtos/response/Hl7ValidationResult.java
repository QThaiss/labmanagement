/*
 * @ {#} Hl7ValidationResult.java   1.0     23/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.dtos.response;

import lombok.Builder;
import lombok.Data;

/*
 * @description: DTO for HL7 validation result
 * @author: Tran Hien Vinh
 * @date:   23/10/2025
 * @version:    1.0
 */
@Data
@Builder
public class Hl7ValidationResult {
    private boolean valid;
    private String errorMessage;
    private String fieldPath;
    private String fieldValue;

    public static Hl7ValidationResult success() {
        return Hl7ValidationResult.builder()
                .valid(true)
                .build();
    }

    public static Hl7ValidationResult error(String fieldPath, String message) {
        return Hl7ValidationResult.builder()
                .valid(false)
                .fieldPath(fieldPath)
                .errorMessage(message)
                .build();
    }
}
