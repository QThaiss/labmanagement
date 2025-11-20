/*
 * @ (#) ApiExceptionHandler.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.exceptions;/*
 * @description: Global exception handler for API requests
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import fit.iam_service.dtos.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        String errorMessage = "Validation failed: " + String.join(", ", errors);
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), errorMessage, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        response.getCode(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        errors
                )
        );
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                req.getRequestURI()
        ));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                req.getRequestURI()
        ));
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(AlreadyExistsException ex, HttpServletRequest req) {
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(
                        response.getCode(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        response.getCode(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid username or password", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(
                        response.getCode(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        List.of("Invalid username or password")
                )
        );
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException ex, HttpServletRequest req) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.LOCKED.value(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.LOCKED).body(
                new ErrorResponse(
                        response.getCode(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex, HttpServletRequest req) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(
                        response.getCode(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<ErrorResponse> handlePasswordException(PasswordException ex, HttpServletRequest req) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        response.getCode(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest req) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(), req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(
                        response.getCode(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }


    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(Exception ex, HttpServletRequest req) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.FORBIDDEN.value(),
                "You do not have permission to access this resource.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse(
                        response.getCode(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormat(InvalidFormatException ex, HttpServletRequest req) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        response.getCode(),
                        response.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        List.of(ex.getMessage())
                )
        );
    }
}
