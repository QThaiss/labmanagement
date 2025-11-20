/*
 * @ {#} ValidateValueFormatException.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.exceptions;

/*
 * @description: Exception thrown when a configuration value does not match the expected format.
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
public class ValidateValueFormatException extends RuntimeException {
    public ValidateValueFormatException(String message) {
        super(message);
    }
}
