/*
 * @ (#) ApiResponse.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final int code;
    private final String message;
    private final T data;
    private final Instant timestamp;
    private final String path;

    public static <T> ApiResponse<T> success(String message, T data, String path) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    public static ApiResponse<Void> success(String message, String path) {
        return success(message, null, path);
    }

    public static <T> ApiResponse<T> error(int code, String message, String path) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .code(401)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> noContent(String message) {
        return ApiResponse.<T>builder()
                .code(204)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
