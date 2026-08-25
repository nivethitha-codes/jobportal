package com.jobportal.service;

import com.jobportal.model.Job;
import com.jobportal.model.SavedJob;
import com.jobportal.model.User;
import com.jobportal.repository.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;

    // Save a job
    public void saveJob(User student, Job job) {
        if (!savedJobRepository.existsByStudentAndJob(student, job)) {
            SavedJob savedJob = SavedJob.builder()
                .student(student)
                .job(job)
                .build();
            savedJobRepository.save(savedJob);
        }
    }

    // Unsave a job
    @Transactional
    public void unsaveJob(User student, Job job) {
        savedJobRepository.deleteByStudentAndJob(student, job);
    }

    // Toggle save/unsave
    @Transactional
    public boolean toggleSaveJob(User student, Job job) {
        if (savedJobRepository.existsByStudentAndJob(student, job)) {
            savedJobRepository.deleteByStudentAndJob(student, job);
            return false;
        } else {
            SavedJob savedJob = SavedJob.builder()
                .student(student)
                .job(job)
                .build();
            savedJobRepository.save(savedJob);
            return true;
        }
    }

    // Get all saved jobs for a student
    public List<SavedJob> getSavedJobs(User student) {
        return savedJobRepository.findByStudent(student);
    }

    // Check if job is saved
    public boolean isJobSaved(User student, Job job) {
        return savedJobRepository.existsByStudentAndJob(student, job);
    }
}