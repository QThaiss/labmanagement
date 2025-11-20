/*
 * @ {#} BadRequestException.java   1.0     20/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.exceptions;

/*
 * @description: Exception for handling bad requests
 * @author: Tran Hien Vinh
 * @date:   20/11/2025
 * @version:    1.0
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
