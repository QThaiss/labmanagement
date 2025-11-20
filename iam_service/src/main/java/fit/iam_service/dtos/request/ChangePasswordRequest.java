/*
 * @ (#) ChangePasswordRequest.java    1.0    06/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 06/10/2025
 * @version: 1.0
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        // Chính chủ bắt buộc; ADMIN có thể bỏ trống
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 128, message = "New password must be 8-128 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,128}$",
                message = "New password must contain letters and digits")
        String newPassword,

        @NotBlank(message = "Confirm password is required")
        String confirmNewPassword
) {
}
