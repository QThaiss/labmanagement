/*
 * @ {#} RefreshTokenResponse.java   1.0     04/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.dtos.response;

/*
 * @description: DTO for refresh token response
 * @author: Tran Hien Vinh
 * @date:   04/10/2025
 * @version:    1.0
 */
public record RefreshTokenResponse(
        String accessToken,

        String refreshToken
) {}
