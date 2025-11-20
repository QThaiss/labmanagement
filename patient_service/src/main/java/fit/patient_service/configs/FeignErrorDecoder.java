/*
 * @ {#} FeignErrorDecoder.java   1.0     08/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.configs;

import feign.Response;
import feign.codec.ErrorDecoder;
import fit.patient_service.exceptions.FeignClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/*
 * @description: Custom error decoder for Feign clients to handle specific HTTP status codes
 * @author: Tran Hien Vinh
 * @date:   08/10/2025
 * @version:    1.0
 */
@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());

        switch (status) {
            case UNAUTHORIZED -> {
                log.warn("[FeignClient] 401 Unauthorized when calling {}", methodKey);
                return new FeignClientException("Unauthorized: Token invalid or expired");
            }
            case FORBIDDEN -> {
                log.warn("[FeignClient] 403 Forbidden when calling {}", methodKey);
                return new FeignClientException("Forbidden: You do not have permission to access this resource");
            }
            case NOT_FOUND -> {
                log.warn("[FeignClient] 404 Not Found when calling {}", methodKey);
                return new FeignClientException("Resource not found in target service");
            }
            default -> {
                log.error("[FeignClient] {} returned {} {}", methodKey, response.status(), response.reason());
                return new FeignClientException("Internal error when calling remote service (status " + response.status() + ")");
            }
        }
    }
}
