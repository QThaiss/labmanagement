/*
 * @ {#} InvalidSortFieldException.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.exceptions;

/*
 * @description: Exception thrown when an invalid sort field is provided
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
public class InvalidSortFieldException extends RuntimeException {
    public InvalidSortFieldException(String message) {
        super(message);
    }
}
