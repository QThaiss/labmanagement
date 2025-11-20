/*
 * @ {#} NotFoundException.java   1.0     27/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.exceptions;

/*
 * @description: Custom exception for not found resources
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
