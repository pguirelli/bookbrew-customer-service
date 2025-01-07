package com.bookbrew.customer.service.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ValidationError {

    private List<String> errors = new ArrayList<>();
    private LocalDateTime timestamp;
    private String message;
    private String path;

    public ValidationError(String message, List<String> errors, String path) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.errors = errors;
        this.path = path;
    }

    public void addError(String field, String message) {
        errors.add(field + ": " + message);
    }

    public List<String> getErrors() {
        return errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
