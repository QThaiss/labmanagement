/*
 * @ {#} RefreshTokenRequest.java   1.0     04/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.dtos.request;

/*
 * @description: DTO for refresh token request
 * @author: Tran Hien Vinh
 * @date:   04/10/2025
 * @version:    1.0
 */

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token must not be blank")
        String refreshToken
) {}

