/*
 * @ {#} LoginRequest.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.dtos.request;

import jakarta.validation.constraints.NotBlank;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
public record LoginRequest(
        @NotBlank(message = "Username must not be blank")
        String username,

        @NotBlank(message = "Password must not be blank")
        String password
) {}
