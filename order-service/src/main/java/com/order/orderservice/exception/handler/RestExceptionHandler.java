package com.order.orderservice.exception.handler;

import com.order.orderservice.exception.InventoryNotAvailableException;
import com.order.orderservice.exception.ResourceNotFoundException;
import com.order.orderservice.exception.ValidationException;
import com.order.orderservice.exception.response.ApiError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.info("Resource not found: {}", ex.getMessage());
        ApiError apiError = ApiError.builder()
                .status(NOT_FOUND.value())
                .message("Resource not found")
                .debugMessage(ex.getMessage())
                .build();
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