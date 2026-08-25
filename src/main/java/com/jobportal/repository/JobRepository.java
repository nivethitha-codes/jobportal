package com.jobportal.repository;

import com.jobportal.model.Job;
import com.jobportal.model.JobStatus;
import com.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);
    List<Job> findByEmployer(User employer);
    List<Job> findByStatusOrderByPostedAtDesc(JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND " +
           "(:category IS NULL OR j.category = :category) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:experience IS NULL OR j.experience = :experience) AND " +
           "(:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Job> searchJobs(@Param("category") String category,
                         @Param("location") String location,
                         @Param("experience") String experience,
                         @Param("keyword") String keyword);
}