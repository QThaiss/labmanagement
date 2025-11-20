/*
 * @ {#} InvalidTokenException.java   1.0     04/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/*
 * @description: Exception thrown when an invalid token is encountered
 * @author: Tran Hien Vinh
 * @date:   04/10/2025
 * @version:    1.0
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
