/*
<<<<<<< HEAD
 * @ (#) SecurityUtils.java    1.0    13/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.utils;/*
 * @description:
 * @author: Bao Thong
 * @date: 13/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.exceptions.UnauthorizedException;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@UtilityClass
public class SecurityUtils {
    /**
     * Lấy ID của người dùng hiện tại từ Security Context.
     * ID này thường được trích xuất từ JWT token sau khi xác thực.
     *
     * @return String ID của người dùng, hoặc null nếu không có ai đăng nhập.
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return jwt.getSubject(); // "sub" = userId
    }
}
