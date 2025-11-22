/*
 * @ {#} AutoCreateTestOrderRequest.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description: DTO for auto-creating test orders based on barcode validation
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoCreateTestOrderRequest {
    @NotBlank(message = "Barcode is required")
    private String barcode;

    // có thể cho client gửi hoặc mặc định true luôn
    private boolean autoCreated = true;

    private boolean requiresPatientMatch = true;
}
