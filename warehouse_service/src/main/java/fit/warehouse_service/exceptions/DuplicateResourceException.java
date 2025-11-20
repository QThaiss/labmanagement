/*
 * @ (#) DuplicateResourceException.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.exceptions;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
