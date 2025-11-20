/*
 * @ {#} UnauthorizedException.java   1.0     13/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.exceptions;

/*
 * @description: Custom exception for unauthorized access scenarios
 * @author: Tran Hien Vinh
 * @date:   13/10/2025
 * @version:    1.0
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
