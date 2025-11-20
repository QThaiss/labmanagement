/*
 * @ {#} LogoutRequest.java   1.0     06/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.dtos.request;

import jakarta.validation.constraints.NotBlank;

/*
 * @description: DTO for logout request
 * @author: Tran Hien Vinh
 * @date:   06/10/2025
 * @version:    1.0
 */
public record LogoutRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken
) {}
