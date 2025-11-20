/*
 * @ {#} RequestParamValidator.java   1.0     16/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package fit.patient_service.validators;

import fit.patient_service.exceptions.InvalidRequestParamException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.util.Set;
import java.util.stream.Collectors;

/*
 * @description: 
 * @author: Tran Hien Vinh
 * @date:   16/10/2025
 * @version:    1.0
 */
@UtilityClass
public class RequestParamValidator {
    private static final Set<String> ALLOWED_PARAMS = Set.of(
            "createdAt",  "gender"
    );

    public void validate(HttpServletRequest request) {
        Set<String> requestParams = request.getParameterMap().keySet();

        Set<String> invalidParams = requestParams.stream()
                .filter(param -> !ALLOWED_PARAMS.contains(param))
                .collect(Collectors.toSet());

        if (!invalidParams.isEmpty()) {
            throw new InvalidRequestParamException(
                    String.format("Invalid query parameter(s): %s. Allowed parameters are: %s",
                            invalidParams, ALLOWED_PARAMS)
            );
        }
    }
}
