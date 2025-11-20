/*
 * @ {#} EmailService.java   1.0     05/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.services;

import fit.iam_service.entities.User;

/*
 * @description: Service interface for handling email-related operations
 * @author: Tran Hien Vinh
 * @date:   05/10/2025
 * @version:    1.0
 */
public interface EmailService {
    void sendPasswordResetEmail(User user, String resetToken);

    void sendEmailVerifyOtp(User user, String otp);
}
