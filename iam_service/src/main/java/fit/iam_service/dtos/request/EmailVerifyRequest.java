/*
 * @ (#) EmailVerifyRequest.java    1.0    06/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 06/10/2025
 * @version: 1.0
 */

import jakarta.validation.constraints.NotBlank;

public record EmailVerifyRequest(
        @NotBlank String userId,
        @NotBlank String otp
) {
}
