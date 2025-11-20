/*
 * @ {#} AccountLockedException.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.exceptions;

/*
 * @description: Exception thrown when an account is locked
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */

public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) {
        super(message);
    }
}
