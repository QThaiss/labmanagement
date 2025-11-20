/*
 * @ {#} AuthServiceImpl.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.services.impl;

import fit.iam_service.dtos.request.*;
import fit.iam_service.dtos.response.*;
import fit.iam_service.entities.AuthSession;
import fit.iam_service.entities.PasswordHistory;
import fit.iam_service.entities.PasswordResetRequest;
import fit.iam_service.entities.User;
import fit.iam_service.exceptions.InvalidTokenException;
import fit.iam_service.exceptions.NotFoundException;
import fit.iam_service.exceptions.UnauthorizedException;
import fit.iam_service.mappers.AuthMapper;
import fit.iam_service.repositories.PasswordHistoryRepository;
import fit.iam_service.repositories.PasswordResetRequestRepository;
import fit.iam_service.repositories.UserRepository;
import fit.iam_service.security.UserDetailsImpl;
import fit.iam_service.security.jwt.JwtUtils;
import fit.iam_service.services.AuditLogService;
import fit.iam_service.services.AuthService;
import fit.iam_service.services.AuthSessionService;
import fit.iam_service.services.EmailService;
import fit.iam_service.utils.HashRefreshToken;
import fit.iam_service.validators.AuthValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/*
 * @description: Service implementation for handling authentication operations
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    private final JwtUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

    private final AuthValidator authValidator;

    private final AuthMapper authMapper;

    private final AuthSessionService authSessionService;

    private final AuditLogService auditLogService;

    private final PasswordResetRequestRepository passwordResetRequestRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final PasswordHistoryRepository passwordHistoryRepository;

    @Transactional
    @Override
    public LoginResponse login(LoginRequest loginRequest, String clientIp) {
        // Validate login request
        authValidator.validateLoginRequest(loginRequest);

         // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        // Set authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create user principal and generate tokens
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken = jwtUtils.generateAccessToken(userPrincipal);
        String refreshToken = jwtUtils.generateRefreshToken(userPrincipal);

        // Get JTI from refresh token
        String jti = jwtUtils.getJtiFromToken(refreshToken);
        LocalDateTime refreshTokenExpiry = jwtUtils.getExpirationFromToken(refreshToken);

        // Extract single role (strip ROLE_ prefix for cleaner display)
        String role = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("");

        // Collect privileges only
        List<String> privilegeList = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .toList();

        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new NotFoundException("User not found with id: " + userPrincipal.getId()));

        // Update user login information
        updateUserLoginInfo(user, clientIp);

        // Create auth session
        String refreshTokenHash = HashRefreshToken.hashRefreshToken(refreshToken);
        authSessionService.createSession(user, jti, refreshTokenHash, clientIp, refreshTokenExpiry);

        // Log user login
        auditLogService.logUserLogin(user, clientIp);

        return authMapper.mapToLoginResponse(user, accessToken, refreshToken, role, privilegeList);
    }

    // Update user's last login time and IP address
    private void updateUserLoginInfo(User user, String clientIp) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        user.setLastLoginAt(now);
        user.setLastLoginIp(clientIp);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request, String clientIp) {
        String raw = request.refreshToken();

        // Validate refresh token and get associated session
        AuthSession authSession = authValidator.validateRefreshToken(raw);

        User user = authSession.getUser();

        String oldJti = authSession.getJti();

        // Revoke old session
        authSessionService.revoke(authSession);

        // Create Response
        RefreshTokenResponse response = authMapper.buildRefreshTokenResponse(user, clientIp);

        // Extract new JTI from response for audit logging
        String newJti = jwtUtils.getJtiFromToken(response.refreshToken());

        // Log refresh token operation
        auditLogService.logRefreshToken(user, clientIp, oldJti, newJti);

        // Create Response
        return response;
    }

    @Transactional
    @Override
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request, String clientIp) {
        // Get email
        String email = request.email().trim();

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email does not exist. Please sign up first."));

        // Check if account is deleted
        if (user.isDeleted()) {
            throw new NotFoundException("Email does not exist. Please sign up first.");
        }

        // Generate raw token and its hash
        String rawToken = UUID.randomUUID().toString().replace("-", "");

        // Hash the token for secure storage
        String tokenHash = HashRefreshToken.hashRefreshToken(rawToken);

        // Create and save password reset request
        PasswordResetRequest prr = PasswordResetRequest.builder()
                .user(user)
                .email(user.getEmail())
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15))
                .requestedIp(clientIp)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        passwordResetRequestRepository.save(prr);

        // Send email with the raw token
        emailService.sendPasswordResetEmail(user, rawToken);

        // Log forgot password
        auditLogService.logForgotPassword(user, clientIp);

        return authMapper.buildForgotResponse();
    }

    @Override
    public PasswordResetRequest validatePasswordResetToken(String token) {
        // Hash the provided token to compare with stored hash
        String tokenHash = HashRefreshToken.hashRefreshToken(token);

        // Find active password reset request by token hash
        PasswordResetRequest prr = passwordResetRequestRepository
                .findActiveByHash(tokenHash, LocalDateTime.now(ZoneOffset.UTC))
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        // Check if the token has already been used
        if (prr.getUsedAt() != null) {
            throw new InvalidTokenException("Reset token already used");
        }

        // Check if the token has expired
        if (prr.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new InvalidTokenException("Reset token expired");
        }

        return prr;
    }

    @Transactional
    @Override
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request, String clientIp) {
        // Validate reset password request
        authValidator.validateResetRequest(request);

        // Validate token
        PasswordResetRequest prr = validatePasswordResetToken(request.token());

        // Get user from the password reset request
        User user = prr.getUser();

        // Ensure new password is not reused
        authValidator.ensurePasswordNotReused(user, request.newPassword());

        // Update user's password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        // Record old password into history
        PasswordHistory ph = PasswordHistory.builder()
                .user(user)
                .oldPasswordHash(user.getPasswordHash())
                .changedBy(user.getUserId())
                .build();
        passwordHistoryRepository.save(ph);

        // Mark the password reset request as used
        prr.setUsedAt(LocalDateTime.now(ZoneOffset.UTC));
        passwordResetRequestRepository.save(prr);

        // Revoke all active sessions for the user
        authSessionService.revokeAllForUser(user);

        // Log password reset event
        auditLogService.logPasswordReset(user, clientIp);

        return authMapper.buildResetResponse();
    }

    @Transactional
    @Override
    public LogoutResponse logoutCurrent(LogoutRequest request, String clientIp) {
        // Validate refresh token and get associated session
        AuthSession session = authValidator.validateRefreshToken(request.refreshToken());

        // Get user from session
        User sessionUser = session.getUser();

        // Get currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Ensure the authenticated user matches the session user
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl principal)) {
            throw new UnauthorizedException("Not authenticated");
        }

        // Extra check to ensure the token belongs to the authenticated user
        if (!principal.getId().equals(sessionUser.getUserId())) {
            throw new InvalidTokenException("Token does not belong to current user");
        }

        // Revoke the session
        authSessionService.revoke(session);

        // Log user logout
        auditLogService.logUserLogout(sessionUser, clientIp, session.getJti(), false);

        return new LogoutResponse("Logged out current session");
    }

    @Transactional
    @Override
    public LogoutResponse logoutAll(String clientIp) {
        // Get currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Ensure user is authenticated
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl principal)) {
            throw new UnauthorizedException("Not authenticated");
        }

        // Get user
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        // Revoke all sessions for the user
        authSessionService.revokeAllForUser(user);

        // Log user logout for all sessions
        auditLogService.logUserLogout(user, clientIp, null, true);

        return new LogoutResponse("Logged out all sessions");
    }
}
