/*
 * @ {#} GlobalExceptionHandler.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.exceptions;

import fit.instrument_service.dtos.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/*
 * @description: Global exception handler for the application
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@RestControllerAdvice
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
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

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
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
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE.value());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new ErrorResponse(
                        response.getStatus(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
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

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExistException(AlreadyExistException ex, HttpServletRequest request) {
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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
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
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
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
