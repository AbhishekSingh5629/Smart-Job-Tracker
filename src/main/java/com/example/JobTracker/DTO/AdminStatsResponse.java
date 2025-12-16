package com.example.JobTracker.DTO;

public class AdminStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long adminUsers;
    private long totalJobs;

    public AdminStatsResponse() {}

    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public long getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(long adminUsers) {
        this.adminUsers = adminUsers;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }
}