/*
 * @ {#} LoginResponse.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.dtos.response;

import java.util.List;

/*
 * @description: Response DTO for login operation
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
public record LoginResponse (
        String userId,

        String username,

        String role,

        List<String> privileges,

        String accessToken,

        String refreshToken
) {}
