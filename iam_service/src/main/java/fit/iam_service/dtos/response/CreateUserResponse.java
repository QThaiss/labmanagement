/*
 * @ (#) CreateUserResponse.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import lombok.Builder;

@Builder
public record CreateUserResponse(
        String userId,
        String email,
        String phone,
        String username,
        String fullName,
        String roleCode
) {
}
