/*
 * @ {#} GlobalExceptionHandler.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.exceptions;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import fit.warehouse_service.dtos.response.ApiResponse;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
 * @description: Global exception handler for the application
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        String errorMessage = "Validation failed: " + String.join(", ", errors);
        ApiResponse<Object> response = ApiResponse.error(errorMessage, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        errors
                )
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NotFoundException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        String specificMessage = "Định dạng body của yêu cầu không hợp lệ.";
        List<String> details = new ArrayList<>();
        Throwable cause = ex.getCause();
        int line = -1;
        int column = -1;
        String fieldPath = ""; // Khởi tạo rỗng

        // Cố gắng lấy thông tin vị trí lỗi và đường dẫn field nếu có
        if (cause instanceof JsonProcessingException jsonEx) {
            JsonLocation location = jsonEx.getLocation();
            if (location != null) {
                line = location.getLineNr();
                column = location.getColumnNr();
            }
            // Chỉ lấy path nếu là JsonMappingException (lỗi liên quan đến cấu trúc/ánh xạ)
            if (cause instanceof JsonMappingException jsonMappingEx) {
                fieldPath = jsonMappingEx.getPath().stream()
                        .map(JsonMappingException.Reference::getFieldName)
                        .filter(name -> name != null)
                        .collect(Collectors.joining("."));
            }
            if (jsonEx.getOriginalMessage() != null && !jsonEx.getOriginalMessage().isEmpty()) {
                details.add(jsonEx.getOriginalMessage());
            }
        }
        // ... (phần xử lý cause khác hoặc ex nếu cần) ...
        else if (cause != null) { /*...*/ } else { /*...*/ }

        // Tạo thông báo lỗi dựa trên loại lỗi, vị trí và field path (nếu có)
        if (cause instanceof InvalidFormatException invalidFormatException) {
            // (Giữ nguyên logic xử lý InvalidFormatException, sử dụng fieldPath nếu cần)
            fieldPath = invalidFormatException.getPath().stream() // Lấy path cụ thể từ lỗi này
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(name -> name != null)
                    .collect(Collectors.joining("."));
            if (invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) { /* ... message cho Enum ... */
                String enumValues = Arrays.stream(((Class<? extends Enum<?>>) invalidFormatException.getTargetType()).getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
                specificMessage = String.format("Giá trị '%s' không hợp lệ cho trường '%s' (dòng %d, cột %d). Các giá trị được chấp nhận là: [%s]", invalidFormatException.getValue(), fieldPath.isEmpty() ? "(không rõ)" : fieldPath, line, column, enumValues);
            } else { /* ... message cho kiểu khác ... */
                String expectedType = invalidFormatException.getTargetType() != null ? invalidFormatException.getTargetType().getSimpleName() : "không xác định";
                String providedValue = invalidFormatException.getValue() != null ? invalidFormatException.getValue().toString() : "null";
                specificMessage = String.format("Giá trị '%s' cung cấp cho trường '%s' không hợp lệ (dòng %d, cột %d). Mong đợi một giá trị kiểu '%s'.", providedValue, fieldPath.isEmpty() ? "(không rõ)" : fieldPath, line, column, expectedType);
            }
        } else if (cause instanceof MismatchedInputException mismatchedInputException) {
            // (Giữ nguyên logic xử lý MismatchedInputException, sử dụng fieldPath nếu cần)
            fieldPath = mismatchedInputException.getPath().stream() // Lấy path cụ thể từ lỗi này
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(name -> name != null)
                    .collect(Collectors.joining("."));
            specificMessage = String.format("Cấu trúc JSON không chính xác gần trường '%s' (dòng %d, cột %d).", fieldPath.isEmpty() ? "(không rõ)" : fieldPath, line, column);

        }
        // --- PHẦN ĐIỀU CHỈNH CHO LỖI CÚ PHÁP CHUNG ---
        else if (cause instanceof JsonParseException || cause instanceof JsonProcessingException) { // Bắt cả JsonParseException cụ thể hơn
            String originalMsg = cause.getMessage(); // Lấy message gốc
            // Chỉ hiển thị vị trí, không hiển thị tên trường không chắc chắn
            if (originalMsg != null && originalMsg.contains("Unexpected character")) {
                specificMessage = String.format("Lỗi cú pháp JSON: Ký tự không mong muốn tại dòng %d, cột %d. Vui lòng kiểm tra định dạng.", line, column);
            } else if (originalMsg != null && originalMsg.contains("was expecting comma")) {
                specificMessage = String.format("Lỗi cú pháp JSON: Thiếu dấu phẩy (,) tại dòng %d, cột %d. Vui lòng kiểm tra định dạng.", line, column);
            } else {
                // Lỗi cú pháp JSON khác
                specificMessage = String.format("Lỗi phân tích cú pháp JSON tại dòng %d, cột %d. Vui lòng kiểm tra định dạng.", line, column);
                // Thêm chi tiết lỗi gốc nếu muốn
                // details.add(originalMsg != null ? originalMsg : "(không có chi tiết)");
            }
        } else {
            // Lỗi không rõ nguyên nhân gốc hoặc không phải từ Jackson
            specificMessage = "Không thể đọc nội dung yêu cầu. Vui lòng kiểm tra định dạng.";
            // details đã được thêm ở trên
        }


        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                specificMessage,
                LocalDateTime.now(),
                request.getDescription(false).substring(4),
                details.isEmpty() ? null : details
        );
        // Cập nhật log nếu muốn
        log.warn("Bad Request (JSON Parse Error): {}, Path: [{}], Location: [{},{}], Details: {}",
                specificMessage, fieldPath.isEmpty() ? "N/A" : fieldPath, line, column, details, ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(Exception ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(
                "You do not have permission to access this resource.",
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(FeignClientException.class)
    public ResponseEntity<ErrorResponse> handleFeignClientException(FeignClientException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_GATEWAY.value());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String parameterName = ex.getName();
        String invalidValue = Objects.requireNonNull(ex.getValue()).toString();
        String requiredType = Objects.requireNonNull(ex.getRequiredType()).getSimpleName();

        String errorMessage;

        // Handle enum type mismatches
        if (ex.getRequiredType().isEnum()) {
            String allowedValues = Arrays.toString(ex.getRequiredType().getEnumConstants());
            errorMessage = String.format(
                    "Invalid value '%s' for parameter '%s'. Allowed values are: %s",
                    invalidValue, parameterName, allowedValues
            );
        }

        // Handle LocalDate type mismatches
        else if (ex.getRequiredType().equals(LocalDate.class)) {
            errorMessage = String.format(
                    "Invalid format for parameter '%s'. Value '%s' could not be converted to type '%s'. Expected format is yyyy-MM-dd.",
                    parameterName, invalidValue, requiredType
            );
        }

        // Handle other type mismatches
        else {
            errorMessage = String.format(
                    "Invalid value '%s' for parameter '%s'. Expected type is '%s'.",
                    invalidValue, parameterName, requiredType
            );
        }

        ApiResponse<Object> response = ApiResponse.error(errorMessage, HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(errorMessage)
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {

        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Invalid parameter value");

        ApiResponse<Object> response = ApiResponse.error(errorMessage, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(errorMessage)
                )
        );
    }

    // --- Handler cho lỗi Database (Data Truncated, Duplicate Entry) ---
    @ExceptionHandler({PersistenceException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleDatabaseConstraintViolation(Exception ex, WebRequest request) {
        String customMessage = "Lỗi hệ thống khi xử lý dữ liệu.";
        List<String> details = new ArrayList<>();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        Throwable rootCause = ex;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        if (rootCause instanceof SQLException sqlEx) {
            String sqlErrorMessage = sqlEx.getMessage();
            details.add(sqlErrorMessage); // Thêm lỗi SQL gốc vào details
            log.warn("SQL Exception occurred: {}", sqlErrorMessage);

            if (sqlErrorMessage != null && sqlErrorMessage.contains("Data truncated for column")) {
                Pattern pattern = Pattern.compile("Data truncated for column '(.+?)' at row");
                Matcher matcher = pattern.matcher(sqlErrorMessage);
                String columnName = "(không xác định)";
                if (matcher.find()) {
                    columnName = matcher.group(1);
                }
                customMessage = String.format("Data truncated for column '%s'", columnName);
                status = HttpStatus.BAD_REQUEST;
            } else if (sqlErrorMessage != null && sqlErrorMessage.contains("Duplicate entry")) {
                customMessage = "Dữ liệu bạn cung cấp bị trùng lặp với dữ liệu đã tồn tại.";
                status = HttpStatus.CONFLICT; // 409
            } else {
                // Các lỗi SQL khác chưa xử lý cụ thể
                customMessage = "Lỗi ràng buộc cơ sở dữ liệu.";
                status = HttpStatus.BAD_REQUEST; // Hoặc INTERNAL_SERVER_ERROR tùy ngữ cảnh
            }
        } else {
            details.add(ex.getMessage()); // Thêm lỗi gốc nếu không phải SQL
            log.error("Unhandled Persistence/DataIntegrity Exception: {}", ex.getMessage(), ex);
        }

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                customMessage,
                LocalDateTime.now(),
                request.getDescription(false).substring(4),
                details // Truyền danh sách details
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Xử lý lỗi khi tài nguyên (ví dụ: Instrument) đã tồn tại.
     *
     * @param ex      Ngoại lệ DuplicateResourceException.
     * @param request Web request hiện tại.
     * @return ResponseEntity chứa ErrorResponse với mã lỗi 409 Conflict.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex, WebRequest request) {
        String customMessage = ex.getMessage(); // Lấy thông báo từ exception gốc

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(), // Mã lỗi 409
                customMessage,
                LocalDateTime.now(),
                request.getDescription(false).substring(4), // Lấy path
                Collections.singletonList(ex.getMessage()) // Thêm message gốc vào details
        );
        log.warn("Conflict Error: {}", customMessage); // Log lỗi ở mức WARN
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExistsException(
            AlreadyExistsException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    /**
     * Handler cho ResourceNotFoundException (lỗi 404 khi không tìm thấy tài nguyên cụ thể).
     * Ghi đè lỗi 404 mặc định của Spring Boot (có "trace") bằng ErrorResponse tùy chỉnh.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage()) // Chỉ bao gồm thông báo lỗi, không bao gồm stack trace
                )
        );
    }

    @ExceptionHandler(ValidateValueFormatException.class)
    public ResponseEntity<ErrorResponse> handleValidateValueFormatException(
            ValidateValueFormatException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSortFieldException(
            InvalidSortFieldException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }
}
