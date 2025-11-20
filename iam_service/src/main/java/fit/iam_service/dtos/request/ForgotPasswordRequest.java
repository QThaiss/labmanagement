/*
 * @ {#} ForgotPasswordRequest.java   1.0     05/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.dtos.request;

import jakarta.validation.constraints.NotBlank;

/*
 * @description: DTO for forgot password request
 * @author: Tran Hien Vinh
 * @date:   05/10/2025
 * @version:    1.0
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "Email  is required")
        String email
) {}
