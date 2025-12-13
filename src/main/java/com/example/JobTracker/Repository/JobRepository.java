package com.example.JobTracker.Repository;


import com.example.JobTracker.Model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByUserId(Long userId);

    List<Job> findByUserIdAndStatus(Long userId, String status);

    List<Job> findByUserIdAndSource(Long userId, String source);

    @Query("SELECT j FROM Job j WHERE j.userId = ?1 AND (j.company LIKE %?2% OR j.role LIKE %?2%)")
    List<Job> searchJobs(Long userId, String keyword);

    @Query("SELECT j.status, COUNT(j) FROM Job j WHERE j.userId = ?1 GROUP BY j.status")
    List<Object[]> getStatusCounts(Long userId);

    @Query("SELECT j.source, COUNT(j) FROM Job j WHERE j.userId = ?1 GROUP BY j.source")
    List<Object[]> getSourceCounts(Long userId);
}