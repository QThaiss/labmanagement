/*
 * @ {#} ResetPasswordRequest.java   1.0     05/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * @description: DTO for forgot password request
 * @author: Tran Hien Vinh
 * @date:   05/10/2025
 * @version:    1.0
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Reset token is required")
        String token,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 128, message = "Password must be 8-128 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,128}$",
                message = "Password must have letters and digits, 8-128 chars")
        String newPassword,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {}
