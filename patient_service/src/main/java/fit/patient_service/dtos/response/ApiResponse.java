/*
 * @ {#} ApiResponse.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

/*
 * @description: Standard API response wrapper
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private int status;
    private String path;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Completed successfully");
    }

    public static <T> ApiResponse<T> error(String message, int status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .status(status)
                .build();
    }

    public static <T> ApiResponse<T> unauthorized(String message, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(401)
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> noContent(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(204)
                .build();
    }
}
