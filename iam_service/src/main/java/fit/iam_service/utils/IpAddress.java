/*
 * @ {#} IpAddress.java   1.0     02/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

/*
 * @description: Utility class for extracting client IP address from HttpServletRequest
 * @author: Tran Hien Vinh
 * @date:   02/10/2025
 * @version:    1.0
 */
@UtilityClass
public class IpAddress {
    public static String clientIp(HttpServletRequest http) {
        String xff = http.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return http.getRemoteAddr();
    }
}
