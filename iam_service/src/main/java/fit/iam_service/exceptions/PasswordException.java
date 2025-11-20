/*
 * @ {#} PasswordInHistoryException.java   1.0     05/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.exceptions;

/*
 * @description: Exception thrown when a new password matches one in the user's password history
 * @author: Tran Hien Vinh
 * @date:   05/10/2025
 * @version:    1.0
 */
public class PasswordException extends RuntimeException {
    public PasswordException(String message) {
        super(message);
    }
}
