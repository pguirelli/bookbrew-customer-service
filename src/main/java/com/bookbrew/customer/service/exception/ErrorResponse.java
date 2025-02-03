package com.bookbrew.customer.service.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String message;

    private LocalDateTime timestamp;

    private String details;

    private String path;

    public ErrorResponse(String message, String details, String path) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.details = details;
        this.path = path;
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
