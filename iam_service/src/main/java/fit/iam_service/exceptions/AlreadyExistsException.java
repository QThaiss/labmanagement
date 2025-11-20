/*
 * @ {#} PhoneAlreadyExistsException.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.exceptions;

/*
 * @description: Custom exception to indicate that an entity already exists in the system
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}
