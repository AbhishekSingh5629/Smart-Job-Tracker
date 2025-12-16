package com.example.JobTracker.DTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ErrorResponse {
    private boolean success;
    private String message;
    private Map<String, String> errors;
    private int status;
    private LocalDateTime timestamp;
    private String path;

    public ErrorResponse() {
        this.success = false;
        this.timestamp = LocalDateTime.now();
        this.errors = new HashMap<>();
    }

    public ErrorResponse(String message, int status, String path) {
        this();
        this.message = message;
        this.status = status;
        this.path = path;
    }

    public ErrorResponse(String message, Map<String, String> errors, int status, String path) {
        this();
        this.message = message;
        this.errors = errors;
        this.status = status;
        this.path = path;
    }

    // Add single error
    public void addError(String field, String errorMessage) {
        this.errors.put(field, errorMessage);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}