/*
 * @ (#) UserService.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import fit.iam_service.dtos.PageResult;
import fit.iam_service.dtos.request.*;
import fit.iam_service.dtos.response.*;
import fit.iam_service.entities.User;

public interface UserService {
    CreateUserResponse create(CreateUserRequest req, String clientIp, String userAgent);

    UpdateUserResponse update(User authUser, String userId, UpdateUserRequest req, String clientIp, String userAgent);

    PageResult<UserListItem> list(UserListQuery query);

    DeleteUserResult deleteUser(User authUser, String targetUserId);

    ChangePasswordResponse changePassword(
            User authUser, String targetUserId, ChangePasswordRequest req, String clientIp, String userAgent);

    UserDetailResponse viewDetail(User authUser, String targetUserId, String clientIp, String userAgent);

    EmailVerifyResponse verifyEmail(EmailVerifyRequest req, String clientIp, String userAgent);
}