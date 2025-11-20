/*
 * @ {#} AuthService.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.services;

import fit.iam_service.dtos.request.*;
import fit.iam_service.dtos.response.*;
import fit.iam_service.entities.PasswordResetRequest;

/*
 * @description: Service interface for handling authentication operations
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
public interface AuthService {
    LoginResponse login(LoginRequest loginRequest, String clientIp);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request, String clientIp);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request, String clientIp);

    PasswordResetRequest validatePasswordResetToken(String token);

    ResetPasswordResponse resetPassword(ResetPasswordRequest request, String clientIp);

    LogoutResponse logoutCurrent(LogoutRequest request, String clientIp);

    LogoutResponse logoutAll(String clientIp);
}
