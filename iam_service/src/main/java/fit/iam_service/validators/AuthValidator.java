/*
 * @ {#} AuthValidator.java   1.0     02/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.validators;

import fit.iam_service.dtos.request.LoginRequest;
import fit.iam_service.dtos.request.ResetPasswordRequest;
import fit.iam_service.entities.AuthSession;
import fit.iam_service.entities.PasswordHistory;
import fit.iam_service.entities.User;
import fit.iam_service.exceptions.AccountLockedException;
import fit.iam_service.exceptions.InvalidTokenException;
import fit.iam_service.exceptions.NotFoundException;
import fit.iam_service.exceptions.PasswordException;
import fit.iam_service.repositories.PasswordHistoryRepository;
import fit.iam_service.repositories.UserRepository;
import fit.iam_service.security.jwt.JwtUtils;
import fit.iam_service.services.AuthSessionService;
import fit.iam_service.utils.HashRefreshToken;
import fit.iam_service.utils.RsaDecryptUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Validator for authentication requests
 * @author: Tran Hien Vinh
 * @date:   02/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class AuthValidator {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    private final AuthSessionService authSessionService;

    private final PasswordHistoryRepository passwordHistoryRepository;

    private static final int PASSWORD_HISTORY_CHECK_COUNT = 5;

    private final RsaDecryptUtils rsaDecryptUtils;

    public void validateLoginRequest(LoginRequest loginRequest) {
        // Check if username exists
        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> new NotFoundException("Username does not exist. Please sign up first."));

        // Check if account is deleted
        if (user.isDeleted()) {
            throw new AccountLockedException("Account has been deactivated.");
        }

        // Check if account is temporarily locked
        if (user.getLockedUntil() != null &&
                user.getLockedUntil().isAfter(LocalDateTime.now(java.time.ZoneOffset.UTC))) {
            throw new AccountLockedException("Account is temporarily locked until: " + user.getLockedUntil());
        }
    }

    public AuthSession validateRefreshToken(String raw) {
        // Validate token structure
        if (!jwtUtils.validateStructureAndSignature(raw)) {
            throw new InvalidTokenException("Invalid refresh token structure");
        }

        // Validate token type
        if (!"refresh".equals(jwtUtils.getType(raw))) {
            throw new InvalidTokenException("Token is not a refresh token");
        }

        // Check expiry
        if (jwtUtils.isExpired(raw)) {
            throw new InvalidTokenException("Refresh token expired");
        }

        // Get JTI
        String jti = jwtUtils.getJtiFromToken(raw);

        // Find session
        AuthSession authSession = authSessionService.findByJti(jti);
        if (!authSession.isActive()) {
            throw new InvalidTokenException("Refresh session inactive");
        }

        // Compare token hash (constant time)
        String providedHash = HashRefreshToken.hashRefreshToken(raw);
        if (!constantTimeEquals(providedHash, authSession.getRefreshTokenHash())) {
            throw new InvalidTokenException("Refresh token mismatch");
        }

        // Check user
        User user = authSession.getUser();
        if (user.isDeleted()) {
            throw new AccountLockedException("User disabled");
        }

        return authSession;
    }

    // Compare two strings in a runtime-independent way that does not depend on different character positions.
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false; // Lengths differ, cannot be equal
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i); // XOR each character and OR the result
        }
        return r == 0; // If r is 0, strings are equal
    }

    // Ensure new password is not reused
    public void ensurePasswordNotReused(User user, String rawNewPassword) {
        // Check current password
        if (passwordEncoder.matches(rawNewPassword, user.getPasswordHash())) {
            throw new PasswordException("New password must differ from current password");
        }
        // Check last N historical passwords
        List<PasswordHistory> recent = passwordHistoryRepository.findTop5ByUserOrderByChangedAtDesc(user);
        for (PasswordHistory h : recent) {
            if (passwordEncoder.matches(rawNewPassword, h.getOldPasswordHash())) {
                throw new PasswordException(
                        "New password must not match any of the last " + PASSWORD_HISTORY_CHECK_COUNT + " passwords"
                );
            }
        }
    }

    // Validate reset password request
    public void validateResetRequest(ResetPasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new PasswordException("New password and confirm password do not match");
        }
    }
}
