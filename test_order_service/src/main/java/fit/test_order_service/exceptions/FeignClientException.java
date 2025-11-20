/*
 * @ {#} FeignClientException.java   1.0     08/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.exceptions;

public class FeignClientException extends RuntimeException {
    public FeignClientException(String message) {
        super(message);
    }
}