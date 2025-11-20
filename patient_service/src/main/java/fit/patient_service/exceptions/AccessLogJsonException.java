/*
 * @ {#} AccessLogJsonException.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.exceptions;

/*
 * @description: Custom exception for JSON processing errors in AccessLogService
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
public class AccessLogJsonException extends RuntimeException {
    public AccessLogJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
