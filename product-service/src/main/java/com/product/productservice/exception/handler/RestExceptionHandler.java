package com.product.productservice.exception.handler;


import com.product.productservice.exception.ResourceNotFoundException;
import com.product.productservice.exception.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleValidationException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError apiError = ApiError.builder()
                .status(NOT_FOUND.value())
                .message("Resource not found")
                .debugMessage(ex.getMessage()).build();
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex) {
        log.error("Unexpected server error: {}", ex.getMessage(), ex);
        ApiError apiError = ApiError.builder()
                .status(INTERNAL_SERVER_ERROR.value())
                .message("General server exception - " + ex.getMessage())
                .debugMessage(Objects.isNull(ex.getCause()) ? null : ex.getCause().toString()).build();
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

}