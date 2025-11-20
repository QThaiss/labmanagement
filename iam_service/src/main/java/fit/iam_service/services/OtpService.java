/*
 * @ (#) OtpService.java    1.0    06/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 06/10/2025
 * @version: 1.0
 */

import fit.iam_service.entities.User;

public interface OtpService {
    void createAndSendEmailVerifyOtp(User user);

    // xác minh OTP email, trả về true nếu thành công
    boolean verifyEmailOtp(User user, String rawOtp);

}