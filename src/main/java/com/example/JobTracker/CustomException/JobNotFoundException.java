package com.example.JobTracker.CustomException;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String message) {
        super(message);
    }

    public JobNotFoundException(Long id) {
        super("Job not found with id: " + id);
    }
}