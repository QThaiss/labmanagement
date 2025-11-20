/*
 * @ {#} UnauthorizedException.java   1.0     08/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.exceptions;

/*
 * @description: Custom exception for unauthorized access
 * @author: Tran Hien Vinh
 * @date:   08/10/2025
 * @version:    1.0
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
