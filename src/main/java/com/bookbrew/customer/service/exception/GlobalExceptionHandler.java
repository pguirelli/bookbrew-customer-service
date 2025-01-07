package com.bookbrew.customer.service.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ValidationError> handleValidationExceptions(ConstraintViolationException ex,
                        WebRequest request) {
                ValidationError error = new ValidationError(
                                "Validation failed for fields",
                                ex.getConstraintViolations()
                                                .stream()
                                                .map(violation -> violation.getPropertyPath().toString() + ": "
                                                                + violation.getMessage())
                                                .collect(Collectors.toList()),
                                request.getDescription(false));

                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                "Resource Not Found",
                                ex.getMessage(),
                                request.getDescription(false));
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequestException(
                        BadRequestException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                "Bad Request",
                                ex.getMessage(),
                                request.getDescription(false));
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                        Exception ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                "Internal Server Error",
                                ex.getMessage(),
                                request.getDescription(false));
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(DuplicateAddressException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateAddressException(
                        DuplicateAddressException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                "Duplicate Address",
                                ex.getMessage(),
                                request.getDescription(false));
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(FeignException.class)
        public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, WebRequest request)
                        throws JsonMappingException, JsonProcessingException {
                ErrorResponse errorResponse = null;
                if (ex.status() == 400) {
                        errorResponse = new ErrorResponse("Validation Error",
                                        new ObjectMapper().readTree(ex.contentUTF8()).get("details").asText(),
                                        request.getDescription(false));

                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
