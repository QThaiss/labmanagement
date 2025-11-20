/*
 * @ {#} BadRequestException.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.exceptions;

/*
 * @description: Custom exception for bad requests
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
