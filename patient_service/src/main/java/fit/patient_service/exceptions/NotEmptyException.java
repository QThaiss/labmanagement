/*
 * @ {#} NotFoundException.java   1.0     27/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.exceptions;

/*
 * @description: Custom exception for not empty constraints
 * @author: Tran Hien Vinh
 * @date:   27/09/2025
 * @version:    1.0
 */
public class NotEmptyException extends RuntimeException {
    public NotEmptyException(String message) {
        super(message);
    }
}
