/*
 * @ {#} InternalUserController.java   1.0     07/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.internal_controllers;

import fit.iam_service.dtos.response.ApiResponse;
import fit.iam_service.dtos.response.UserDetailResponse;
import fit.iam_service.entities.User;
import fit.iam_service.exceptions.NotFoundException;
import fit.iam_service.repositories.UserRepository;
import fit.iam_service.security.UserDetailsImpl;
import fit.iam_service.services.UserService;
import fit.iam_service.utils.IpAddress;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @description: Controller for internal user operations
 * @author: Tran Hien Vinh
 * @date:   07/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/internal/iam/users")
@RequiredArgsConstructor
public class InternalUserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or #id == principal.id")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getDetail(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetailsImpl principal,
            HttpServletRequest http
    ) {
        User authUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        UserDetailResponse data = userService.viewDetail(
                authUser, id, IpAddress.clientIp(http), http.getHeader("User-Agent"));

        return ResponseEntity.ok(ApiResponse.success("Xem thông tin tài khoản thành công.", data, http.getRequestURI()));
    }
}
