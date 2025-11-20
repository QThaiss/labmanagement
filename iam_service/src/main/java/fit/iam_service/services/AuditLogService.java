/*
 * @ {#} AuditLogService.java   1.0     02/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.services;

import fit.iam_service.entities.User;

/*
 * @description: Service interface for logging audit events
 * @author: Tran Hien Vinh
 * @date:   02/10/2025
 * @version:    1.0
 */
public interface AuditLogService {
    void logUserLogin(User user, String ip);

    void logRefreshToken(User user, String ip, String oldJti, String newJti);

    void logForgotPassword(User user, String ip);

    void logPasswordReset(User user, String ip);

    void logUserLogout(User user, String ip, String jti, boolean allSessions);
}
