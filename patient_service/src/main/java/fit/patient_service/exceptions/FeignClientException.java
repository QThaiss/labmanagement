/*
 * @ {#} FeignClientException.java   1.0     08/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.exceptions;

/*
 * @description: Custom exception for Feign client errors
 * @author: Tran Hien Vinh
 * @date:   08/10/2025
 * @version:    1.0
 */
public class FeignClientException extends RuntimeException {
    public FeignClientException(String message) {
        super(message);
    }
}