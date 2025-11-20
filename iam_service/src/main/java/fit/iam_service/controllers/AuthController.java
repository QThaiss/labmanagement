/*
 * @ {#} AuthController.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.controllers;

import fit.iam_service.dtos.request.*;
import fit.iam_service.dtos.response.*;
import fit.iam_service.entities.PasswordResetRequest;
import fit.iam_service.services.AuthService;
import fit.iam_service.services.RsaKeyPairService;
import fit.iam_service.utils.IpAddress;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/*
 * @description: Controller for handling authentication-related requests
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/iam/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    private final RsaKeyPairService rsaService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest http) {
        LoginResponse loginResponse = authService.login(loginRequest, IpAddress.clientIp(http));
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest req, HttpServletRequest http) {
        RefreshTokenResponse refreshTokenResponse = authService.refreshToken(req, IpAddress.clientIp(http));
        return ResponseEntity.ok(ApiResponse.success(refreshTokenResponse, "Token refreshed successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req, HttpServletRequest http) {
        ForgotPasswordResponse resp = authService.forgotPassword(req, IpAddress.clientIp(http));
        return ResponseEntity.ok(ApiResponse.noContent(resp.message()));
    }

    @GetMapping("/password/{token}")
    public ResponseEntity<ApiResponse<Void>> validateToken(@PathVariable("token") String token) {
        PasswordResetRequest passwordResetRequest = authService.validatePasswordResetToken(token);
        return ResponseEntity.ok(ApiResponse.success(passwordResetRequest.getUser().getUserId(), "Token is valid."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest req, HttpServletRequest http) {
        ResetPasswordResponse resp = authService.resetPassword(req, IpAddress.clientIp(http));
        return ResponseEntity.ok(ApiResponse.noContent(resp.message()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logoutCurrent(@Valid @RequestBody LogoutRequest req, HttpServletRequest http) {
        LogoutResponse data = authService.logoutCurrent(req, IpAddress.clientIp(http));
        return ResponseEntity.ok(ApiResponse.noContent(data.message()));
    }

    @PostMapping("/logout/all")
    public ResponseEntity<ApiResponse<LogoutResponse>> logoutAll(HttpServletRequest http) {
        LogoutResponse data = authService.logoutAll(IpAddress.clientIp(http));
        return ResponseEntity.ok(ApiResponse.noContent(data.message()));
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", rsaService.getPublicKeyBase64()));
    }

    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, Object>> encrypt(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        try {
            Map<String, Object> result = rsaService.encryptPassword(password);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
