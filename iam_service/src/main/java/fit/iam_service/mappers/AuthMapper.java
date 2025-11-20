/*
 * @ {#} AuthMapper.java   1.0     02/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.mappers;

import fit.iam_service.dtos.response.ForgotPasswordResponse;
import fit.iam_service.dtos.response.LoginResponse;
import fit.iam_service.dtos.response.RefreshTokenResponse;
import fit.iam_service.dtos.response.ResetPasswordResponse;
import fit.iam_service.entities.User;
import fit.iam_service.security.UserDetailsImpl;
import fit.iam_service.security.jwt.JwtUtils;
import fit.iam_service.services.AuthSessionService;
import fit.iam_service.utils.HashRefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Mapper class for authentication-related data transformations
 * @author: Tran Hien Vinh
 * @date:   02/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class AuthMapper {
    private final JwtUtils jwtUtils;

    private final AuthSessionService authSessionService;

    // Map User entity and tokens to LoginResponse DTO
    public LoginResponse mapToLoginResponse(User user, String accessToken, String refreshToken, String roles, List<String> privileges) {
        return new LoginResponse(
                user.getUserId(),
                user.getUsername(),
                roles,
                privileges,
                accessToken,
                refreshToken

        );
    }

    // Build RefreshTokenResponse DTO and create new AuthSession
    public RefreshTokenResponse buildRefreshTokenResponse(User user, String clientIp) {
        UserDetailsImpl principal = UserDetailsImpl.build(user);

        String newAccess = jwtUtils.generateAccessToken(principal);
        String newRefresh = jwtUtils.generateRefreshToken(principal);

        String newJti = jwtUtils.getJtiFromToken(newRefresh);
        LocalDateTime refreshExpiry = jwtUtils.getExpirationFromToken(newRefresh);

        authSessionService.createSession(
                user,
                newJti,
                HashRefreshToken.hashRefreshToken(newRefresh),
                clientIp,
                refreshExpiry
        );

        return new RefreshTokenResponse(newAccess, newRefresh);
    }

    // Build ForgotPasswordResponse DTO
    public ForgotPasswordResponse buildForgotResponse() {
        return new ForgotPasswordResponse("Password reset email has been sent.");
    }

    // Build ResetPasswordResponse DTO
    public ResetPasswordResponse buildResetResponse() {
        return new ResetPasswordResponse("Password has been reset successfully.");
    }
}
