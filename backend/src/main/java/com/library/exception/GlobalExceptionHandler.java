package com.library.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse>
    handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .timestamp(
                                LocalDateTime.now())
                        .status(404)
                        .error("NOT_FOUND")
                        .message(ex.getMessage())
                        .path(
                                request.getRequestURI())
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(
            DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse>
    handleDuplicate(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .timestamp(
                                LocalDateTime.now())
                        .status(409)
                        .error("CONFLICT")
                        .message(ex.getMessage())
                        .path(
                                request.getRequestURI())
                        .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(
            BusinessException.class)
    public ResponseEntity<ApiErrorResponse>
    handleBusiness(
            BusinessException ex,
            HttpServletRequest request) {

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .timestamp(
                                LocalDateTime.now())
                        .status(400)
                        .error("BAD_REQUEST")
                        .message(ex.getMessage())
                        .path(
                                request.getRequestURI())
                        .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse>
    handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .timestamp(
                                LocalDateTime.now())
                        .status(500)
                        .error(
                                "INTERNAL_SERVER_ERROR")
                        .message(
                                ex.getMessage())
                        .path(
                                request.getRequestURI())
                        .build();

        return ResponseEntity
                .internalServerError()
                .body(response);
    }
}