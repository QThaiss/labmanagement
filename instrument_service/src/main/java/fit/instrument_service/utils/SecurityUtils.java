/*
 * @ {#} SecurityUtils.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.utils;

import fit.instrument_service.exceptions.UnauthorizedException;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/*
 * @description: Utility class for security-related operations
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@UtilityClass
public class SecurityUtils {

    // Get the current authenticated user's ID from the JWT token
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return jwt.getSubject(); // "sub" = userId
    }
}
