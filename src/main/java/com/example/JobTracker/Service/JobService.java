package com.example.JobTracker.Service;


import com.example.JobTracker.DTO.JobRequest;
import com.example.JobTracker.DTO.JobResponse;
import com.example.JobTracker.CustomException.JobNotFoundException;
import com.example.JobTracker.Mapper.JobMapper;
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
    private  JobMapper jobMapper;
    @Autowired
    private JobRepository jobRepository;

    public List<JobResponse> getAllJobsByUserId(Long userId) {
        List<Job> jobs = jobRepository.findByUserId(userId);
        return jobs.stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
        return  jobMapper.toResponse(job);
    }

    public JobResponse addJob(JobRequest dto, Long userId) {
        Job job = new Job();
        job.setUserId(userId);
        job.setCompany(dto.getCompany());
        job.setRole(dto.getRole());
        job.setAppliedDate(dto.getAppliedDate());
        job.setSource(dto.getSource());
        job.setStatus(dto.getStatus() != null ? dto.getStatus() : "Applied");
        job.setDeadline(dto.getDeadline());
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        Job savedJob = jobRepository.save(job);
        return  jobMapper.toResponse(job);
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
        return  jobMapper.toResponse(job);
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
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<JobResponse> searchJobs(Long userId, String keyword) {
        List<Job> jobs = jobRepository.searchJobs(userId, keyword);
        return jobs.stream()
                .map(jobMapper::toResponse)
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
    public Map<String, Object> getUserDashboardStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        List<Job> userJobs = jobRepository.findByUserId(userId);

        // Status counts
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("Applied", userJobs.stream().filter(j -> "Applied".equals(j.getStatus())).count());
        statusCounts.put("Interview", userJobs.stream().filter(j -> "Interview".equals(j.getStatus())).count());
        statusCounts.put("Offer", userJobs.stream().filter(j -> "Offer".equals(j.getStatus())).count());
        statusCounts.put("Rejected", userJobs.stream().filter(j -> "Rejected".equals(j.getStatus())).count());

        // Source counts
        Map<String, Long> sourceCounts = new HashMap<>();
        userJobs.stream().collect(java.util.stream.Collectors.groupingBy(Job::getSource, java.util.stream.Collectors.counting()))
                .forEach(sourceCounts::put);

        // Monthly applications (last 6 months)
        Map<String, Long> monthlyApplications = new HashMap<>();
        java.time.LocalDate now = java.time.LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate month = now.minusMonths(i);
            String monthKey = month.format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy"));
            long count = userJobs.stream()
                    .filter(j -> j.getAppliedDate().getYear() == month.getYear() &&
                            j.getAppliedDate().getMonth() == month.getMonth())
                    .count();
            monthlyApplications.put(monthKey, count);
        }

        stats.put("totalJobs", userJobs.size());
        stats.put("statusCounts", statusCounts);
        stats.put("sourceCounts", sourceCounts);
        stats.put("monthlyApplications", monthlyApplications);
        stats.put("successRate", calculateSuccessRate(statusCounts));

        return stats;
    }

    private double calculateSuccessRate(Map<String, Long> statusCounts) {
        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) return 0.0;
        long successful = statusCounts.getOrDefault("Offer", 0L) + statusCounts.getOrDefault("Interview", 0L);
        return (successful * 100.0) / total;
    }
}