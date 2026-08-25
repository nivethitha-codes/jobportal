package com.jobportal.repository;

import com.jobportal.model.Job;
import com.jobportal.model.SavedJob;
import com.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SavedJobRepository
        extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findByStudent(User student);

    Optional<SavedJob> findByStudentAndJob(User student, Job job);

    boolean existsByStudentAndJob(User student, Job job);

    void deleteByStudentAndJob(User student, Job job);

    // ── NEW ──────────────────────────────────────────────────────
    void deleteByStudent(User student);

    void deleteByJob(Job job);
}