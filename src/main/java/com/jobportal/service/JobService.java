package com.jobportal.service;

import com.jobportal.model.Job;
import com.jobportal.model.JobStatus;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public Job postJob(Job job) {
        return jobRepository.save(job);
    }

    public List<Job> getAllActiveJobs() {
        return jobRepository.findByStatusOrderByPostedAtDesc(JobStatus.ACTIVE);
    }

    public List<Job> searchJobs(String category, String location,
                                 String experience, String keyword) {
        return jobRepository.searchJobs(
            (category != null && !category.isEmpty()) ? category : null,
            (location != null && !location.isEmpty()) ? location : null,
            (experience != null && !experience.isEmpty()) ? experience : null,
            (keyword != null && !keyword.isEmpty()) ? keyword : null
        );
    }

    public List<Job> getJobsByEmployer(User employer) {
        return jobRepository.findByEmployer(employer);
    }

    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    public Job updateJob(Job job) {
        return jobRepository.save(job);
    }
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
}