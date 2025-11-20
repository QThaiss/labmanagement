/*
 * @ {#} InvalidSortFieldException.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.exceptions;

/*
 * @description: Exception thrown when an invalid sort field is provided
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
public class InvalidSortFieldException extends RuntimeException {
    public InvalidSortFieldException(String message) {
        super(message);
    }
}
