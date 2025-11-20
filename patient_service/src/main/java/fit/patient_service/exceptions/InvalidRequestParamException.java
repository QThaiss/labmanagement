/*
 * @ {#} InvalidRequestParamException.java   1.0     16/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package fit.patient_service.exceptions;
/*
 * @description:  Custom exception thrown when an invalid request parameter is encountered.
 * @author: Tran Hien Vinh
 * @date:   16/10/2025
 * @version:    1.0
 */
public class InvalidRequestParamException extends RuntimeException {
    public InvalidRequestParamException(String message) {
        super(message);
    }
}
