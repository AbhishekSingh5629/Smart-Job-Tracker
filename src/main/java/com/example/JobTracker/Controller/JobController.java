package com.example.JobTracker.Controller;

import com.example.JobTracker.DTO.JobRequest;
import com.example.JobTracker.DTO.JobResponse;
import com.example.JobTracker.CustomException.JobNotFoundException;
import com.example.JobTracker.Service.JobService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<JobResponse>> getAllJobs(@PathVariable Long userId) {
        List<JobResponse> jobs = jobService.getAllJobsByUserId(userId);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable Long id) {
        try {
            JobResponse job = jobService.getJobById(id);
            return ResponseEntity.ok(job);
        } catch (JobNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addJob(
            @RequestBody JobRequest dto,
            HttpServletRequest request
    ) {
        try {
            Long userId = (Long) request.getAttribute("userId");

            JobResponse savedJob = jobService.addJob(dto, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job application added successfully");
            response.put("job", savedJob);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateJob(@PathVariable Long id,
                                                         @RequestBody JobRequest dto) {
        try {
            JobResponse updatedJob = jobService.updateJob(id, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job application updated successfully");
            response.put("job", updatedJob);

            return ResponseEntity.ok(response);
        } catch (JobNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteJob(@PathVariable Long id) {
        try {
            jobService.deleteJob(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job application deleted successfully");

            return ResponseEntity.ok(response);
        } catch (JobNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<JobResponse>> getJobsByStatus(@PathVariable Long userId,
                                                             @PathVariable String status) {
        List<JobResponse> jobs = jobService.getJobsByStatus(userId, status);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<JobResponse>> searchJobs(@PathVariable Long userId,
                                                        @RequestParam String keyword) {
        List<JobResponse> jobs = jobService.searchJobs(userId, keyword);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long userId) {
        Map<String, Object> stats = jobService.getStatistics(userId);
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/user/{userId}/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@PathVariable Long userId) {
        Map<String, Object> stats = jobService.getUserDashboardStats(userId);
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Job Tracker API is running");
        return ResponseEntity.ok(response);
    }
}