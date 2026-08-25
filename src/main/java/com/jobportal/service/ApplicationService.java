package com.jobportal.service;

import com.jobportal.model.Application;
import com.jobportal.model.ApplicationStatus;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    public Application apply(Job job, User applicant, String coverLetter) {
        if (applicationRepository.existsByJobAndApplicant(job, applicant))
            throw new RuntimeException("You have already applied for this job!");
        Application app = Application.builder()
            .job(job)
            .applicant(applicant)
            .coverLetter(coverLetter)
            .build();
        return applicationRepository.save(app);
    }

    public List<Application> getApplicationsByStudent(User student) {
        return applicationRepository.findByApplicant(student);
    }

    public List<Application> getApplicationsByJob(Job job) {
        return applicationRepository.findByJob(job);
    }

    public Application updateStatus(Long applicationId,
                                    ApplicationStatus status) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(status);
        Application saved = applicationRepository.save(app);

        // Send email when shortlisted or rejected
        if (status == ApplicationStatus.SHORTLISTED ||
            status == ApplicationStatus.REJECTED) {
            emailService.sendApplicationStatusEmail(
                app.getApplicant().getEmail(),
                app.getApplicant().getFullName(),
                app.getJob().getTitle(),
                app.getJob().getEmployer().getCompanyName(),
                status.name()
            );
        }
        return saved;
    }

    public Map<String, Long> getDashboardStats(Job job) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total",
            applicationRepository.countByJob(job));
        stats.put("shortlisted",
            applicationRepository.countByJobAndStatus(
                job, ApplicationStatus.SHORTLISTED));
        stats.put("rejected",
            applicationRepository.countByJobAndStatus(
                job, ApplicationStatus.REJECTED));
        return stats;
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }
    public void withdrawApplication(Long applicationId, User student) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        // Only PENDING applications can be withdrawn
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException(
                "Only pending applications can be withdrawn!");
        }

        // Security check — only the applicant can withdraw
        if (!app.getApplicant().getId().equals(student.getId())) {
            throw new RuntimeException("Unauthorized action!");
        }

        applicationRepository.delete(app);
    }
}