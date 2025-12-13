package com.example.JobTracker.Service;


import com.example.JobTracker.DTO.JobRequest;
import com.example.JobTracker.DTO.JobResponse;
import com.example.JobTracker.CustomException.JobNotFoundException;
import com.example.JobTracker.Model.Job;
import com.example.JobTracker.Repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public List<JobResponse> getAllJobsByUserId(Long userId) {
        List<Job> jobs = jobRepository.findByUserId(userId);
        return jobs.stream()
                .map(JobResponse::new)
                .collect(Collectors.toList());
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
        return new JobResponse(job);
    }

    public JobResponse addJob(JobRequest dto) {
        Job job = new Job();
        job.setUserId(dto.getUserId());
        job.setCompany(dto.getCompany());
        job.setRole(dto.getRole());
        job.setAppliedDate(dto.getAppliedDate());
        job.setSource(dto.getSource());
        job.setStatus(dto.getStatus() != null ? dto.getStatus() : "Applied");
        job.setDeadline(dto.getDeadline());
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        Job savedJob = jobRepository.save(job);
        return new JobResponse(savedJob);
    }

    public JobResponse updateJob(Long id, JobRequest dto) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));

        job.setCompany(dto.getCompany());
        job.setRole(dto.getRole());
        job.setAppliedDate(dto.getAppliedDate());
        job.setSource(dto.getSource());
        job.setStatus(dto.getStatus());
        job.setDeadline(dto.getDeadline());
        job.setUpdatedAt(LocalDateTime.now());

        Job updatedJob = jobRepository.save(job);
        return new JobResponse(updatedJob);
    }

    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new JobNotFoundException(id);
        }
        jobRepository.deleteById(id);
    }

    public List<JobResponse> getJobsByStatus(Long userId, String status) {
        List<Job> jobs = jobRepository.findByUserIdAndStatus(userId, status);
        return jobs.stream()
                .map(JobResponse::new)
                .collect(Collectors.toList());
    }

    public List<JobResponse> searchJobs(Long userId, String keyword) {
        List<Job> jobs = jobRepository.searchJobs(userId, keyword);
        return jobs.stream()
                .map(JobResponse::new)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistics(Long userId) {
        List<Object[]> statusStats = jobRepository.getStatusCounts(userId);
        List<Object[]> sourceStats = jobRepository.getSourceCounts(userId);

        Map<String, Object> stats = new HashMap<>();

        Map<String, Long> statusCounts = new HashMap<>();
        long total = 0;
        for (Object[] stat : statusStats) {
            String status = (String) stat[0];
            Long count = (Long) stat[1];
            statusCounts.put(status, count);
            total += count;
        }

        Map<String, Long> sourceCounts = new HashMap<>();
        for (Object[] stat : sourceStats) {
            sourceCounts.put((String) stat[0], (Long) stat[1]);
        }

        stats.put("statusCounts", statusCounts);
        stats.put("sourceCounts", sourceCounts);
        stats.put("totalJobs", total);
        stats.put("appliedCount", statusCounts.getOrDefault("Applied", 0L));
        stats.put("interviewCount", statusCounts.getOrDefault("Interview", 0L));
        stats.put("offerCount", statusCounts.getOrDefault("Offer", 0L));
        stats.put("rejectedCount", statusCounts.getOrDefault("Rejected", 0L));

        return stats;
    }
}