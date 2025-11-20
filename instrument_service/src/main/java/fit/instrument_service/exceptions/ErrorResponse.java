/*
 * @ {#} ErrorResponse.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Standard error response structure
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;

    private String message;

    private LocalDateTime timestamp;

    private String path;

    private List<String> details;
}