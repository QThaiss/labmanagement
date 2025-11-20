/*
 * @ {#} HashRefreshToken.java   1.0     04/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.utils;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/*
 * @description: Utility class for hashing refresh tokens
 * @author: Tran Hien Vinh
 * @date:   04/10/2025
 * @version:    1.0
 */
@UtilityClass
public class HashRefreshToken {

    // Hash the refresh token using SHA-256
    public String hashRefreshToken(String refreshToken) {
        // Use SHA-256 instead of BCrypt for long strings like JWT tokens
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
