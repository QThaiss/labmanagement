/*
 * @ (#) PrintTestOrderRequest.java    1.0    22/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 22/10/2025
 * @version: 1.0
 */

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PrintTestOrderRequest {
    // Tên file tùy chỉnh
    @Size(max = 100, message = "Custom file name must not exceed 100 characters.")
    private String customFileName;
}
