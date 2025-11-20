/*
 * @ {#} InvalidSortFieldException.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.exceptions;

/*
 * @description: Exception thrown for invalid sort fields
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
public class InvalidSortFieldException extends RuntimeException {
    public InvalidSortFieldException(String message) {
        super(message);
    }
}
