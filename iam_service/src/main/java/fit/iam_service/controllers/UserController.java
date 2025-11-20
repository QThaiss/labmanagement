/*
 * @ (#) UserController.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.controllers;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import fit.iam_service.dtos.PageResult;
import fit.iam_service.dtos.request.*;
import fit.iam_service.dtos.response.*;
import fit.iam_service.entities.User;
import fit.iam_service.repositories.UserRepository;
import fit.iam_service.security.UserDetailsImpl;
import fit.iam_service.services.OtpService;
import fit.iam_service.services.UserService;
import fit.iam_service.utils.IpAddress;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iam/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final OtpService otpService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateUserResponse>> createUser(@Valid @RequestBody CreateUserRequest req,
                                                                      HttpServletRequest http) {
        var data = userService.create(req, IpAddress.clientIp(http), http.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.success(
                "Create user successfully",
                data,
                http.getRequestURI()
        ));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<EmailVerifyResponse>> verifyEmail(
            @Valid @RequestBody EmailVerifyRequest req,
            HttpServletRequest http
    ) {

        EmailVerifyResponse data = userService.verifyEmail(req, IpAddress.clientIp(http), http.getHeader("User-Agent"));

        return ResponseEntity.ok(ApiResponse.success(
                "Xác minh email thành công",
                data,
                http.getRequestURI()
        ));
    }

    @PostMapping("/email/resend")
    public ResponseEntity<ApiResponse<Void>> resendEmailOtp(
            @Valid @RequestBody EmailResendRequest req,
            HttpServletRequest http
    ) {
        User user = userRepository.findActiveById(req.userId().trim())
                .orElseThrow(() -> new EntityNotFoundException("Tài khoản không tồn tại hoặc đã bị xóa: " + req.userId()));

        otpService.createAndSendEmailVerifyOtp(user);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã gửi lại mã xác minh email. Vui lòng kiểm tra hộp thư.",
                        null,
                        http.getRequestURI()
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateUser(
            @PathVariable("id") String userId,
            @Valid @RequestBody UpdateUserRequest req,
            @AuthenticationPrincipal UserDetailsImpl principal,
            HttpServletRequest http
    ) {
        // principal comes from JwtAuthenticationFilter
        User authUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        UpdateUserResponse data = userService.update(
                authUser, userId, req, IpAddress.clientIp(http), http.getHeader("User-Agent"));

        return ResponseEntity.ok(ApiResponse.success(
                "Update user successfully", data, http.getRequestURI()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can list users
    public ResponseEntity<ApiResponse<PageResult<UserListItem>>> listUsers(UserListQuery q,
                                                                           HttpServletRequest http,
                                                                           @AuthenticationPrincipal UserDetailsImpl principal) {
        // principal comes from JwtAuthenticationFilter
        userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        PageResult<UserListItem> data = userService.list(q);
        return ResponseEntity.ok(
                ApiResponse.success(
                        data.empty() ? "No Data" : "Get users successfully",
                        data,
                        http.getRequestURI()
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can delete users
    public ResponseEntity<ApiResponse<DeleteUserResult>> delete(@PathVariable("id") String userId,
                                                                @AuthenticationPrincipal UserDetailsImpl principal,
                                                                HttpServletRequest http) {
        // principal comes from JwtAuthenticationFilter
        User authUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        DeleteUserResult result = userService.deleteUser(authUser, userId);
        return ResponseEntity.ok(
                ApiResponse.success("Xóa người dùng thành công.", result, http.getRequestURI())
        );
    }

    // --- change password ---
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or #targetUserId == principal.id")
    public ResponseEntity<ApiResponse<ChangePasswordResponse>> changePassword(
            @PathVariable("id") String targetUserId,
            @Valid @RequestBody ChangePasswordRequest req,
            @AuthenticationPrincipal UserDetailsImpl principal,
            HttpServletRequest http
    ) {
        User authUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        ChangePasswordResponse data = userService.changePassword(
                authUser, targetUserId, req, IpAddress.clientIp(http), http.getHeader("User-Agent"));

        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công.", data, http.getRequestURI()));
    }

    // --- view detail ---
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or #id == principal.id")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getDetail(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetailsImpl principal,
            HttpServletRequest http
    ) {
        User authUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        UserDetailResponse data = userService.viewDetail(
                authUser, id, IpAddress.clientIp(http), http.getHeader("User-Agent"));

        return ResponseEntity.ok(ApiResponse.success("Xem thông tin tài khoản thành công.", data, http.getRequestURI()));
    }
}