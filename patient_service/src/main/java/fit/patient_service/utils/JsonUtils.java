/*
 * @ {#} JsonUtils.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.patient_service.exceptions.AccessLogJsonException;
import lombok.experimental.UtilityClass;

import java.util.Map;

/*
 * @description: Utility class for JSON operations
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@UtilityClass
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Convert log map to JSON string
    public String convertToJson(Map<String, Object> logMap) {
        try {
            return objectMapper.writeValueAsString(logMap);
        } catch (JsonProcessingException e) {
            throw new AccessLogJsonException("Error converting log map to JSON", e);
        }
    }
}
