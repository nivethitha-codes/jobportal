package com.jobportal.controller;

import com.jobportal.model.*;
import com.jobportal.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final UserService userService;
    private final ApplicationService applicationService;
    private final SavedJobService savedJobService;

    // ── Browse Jobs ──────────────────────────────────────────────
    @GetMapping("/jobs/browse")
    public String browseJobs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Job> jobs;
        boolean isFiltered = (category != null || location != null ||
                              experience != null || keyword != null);
        jobs = isFiltered
            ? jobService.searchJobs(category, location, experience, keyword)
            : jobService.getAllActiveJobs();

        model.addAttribute("jobs", jobs);
        model.addAttribute("totalJobs", jobs.size());
        return "jobs/browse";
    }

    // ── Job Detail ───────────────────────────────────────────────
    @GetMapping("/jobs/{id}")
    public String jobDetail(@PathVariable Long id, Model model,
                            @AuthenticationPrincipal UserDetails userDetails) {
        Job job = jobService.findById(id).orElseThrow();
        model.addAttribute("job", job);
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("currentUser", user);
                boolean alreadyApplied = applicationService
                    .getApplicationsByStudent(user)
                    .stream().anyMatch(a -> a.getJob().getId().equals(id));
                model.addAttribute("alreadyApplied", alreadyApplied);
                boolean isSaved = savedJobService.isJobSaved(user, job);
                model.addAttribute("isSaved", isSaved);
            });
        }
        return "jobs/detail";
    }

    // ── Apply for Job ────────────────────────────────────────────
    @PostMapping("/student/apply/{jobId}")
    public String applyForJob(@PathVariable Long jobId,
                              @RequestParam String coverLetter,
                              @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        Job job = jobService.findById(jobId).orElseThrow();
        applicationService.apply(job, student, coverLetter);
        return "redirect:/student/applications?success=true";
    }

    // ── Post Job GET ─────────────────────────────────────────────
    @GetMapping("/employer/post-job")
    public String postJobPage(Model model) {
        model.addAttribute("job", new Job());
        return "employer/post-job";
    }

    // ── Post Job POST ────────────────────────────────────────────
    @PostMapping("/employer/post-job")
    public String postJob(@ModelAttribute Job job,
                          @AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        job.setEmployer(employer);
        jobService.postJob(job);
        return "redirect:/employer/dashboard?posted=true";
    }

    // ── View Applicants ──────────────────────────────────────────
    @GetMapping("/employer/applicants/{jobId}")
    public String viewApplicants(@PathVariable Long jobId, Model model) {
        Job job = jobService.findById(jobId).orElseThrow();
        List<Application> applications =
            applicationService.getApplicationsByJob(job);
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("stats",
            applicationService.getDashboardStats(job));
        return "employer/applicants";
    }

    // ── Update Application Status ────────────────────────────────
    @PostMapping("/employer/application/{appId}/status")
    public String updateApplicationStatus(
            @PathVariable Long appId,
            @RequestParam ApplicationStatus status,
            @RequestParam Long jobId) {
        applicationService.updateStatus(appId, status);
        return "redirect:/employer/applicants/" + jobId + "?updated=true";
    }

    // ── Delete Job ───────────────────────────────────────────────
    @PostMapping("/employer/delete-job/{id}")
    public String deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return "redirect:/employer/dashboard?deleted=true";
    }

    // ── Edit Job GET ─────────────────────────────────────────────
    @GetMapping("/employer/edit-job/{id}")
    public String editJobPage(@PathVariable Long id,
                              Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {
        Job job = jobService.findById(id).orElseThrow();
        User employer = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        if (!job.getEmployer().getId().equals(employer.getId())) {
            return "redirect:/employer/dashboard";
        }
        model.addAttribute("job", job);
        return "employer/edit-job";
    }

    // ── Edit Job POST ────────────────────────────────────────────
    @PostMapping("/employer/edit-job/{id}")
    public String editJob(@PathVariable Long id,
                          @ModelAttribute Job updatedJob,
                          @AuthenticationPrincipal UserDetails userDetails) {
        Job job = jobService.findById(id).orElseThrow();
        User employer = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        if (!job.getEmployer().getId().equals(employer.getId())) {
            return "redirect:/employer/dashboard";
        }
        job.setTitle(updatedJob.getTitle());
        job.setDescription(updatedJob.getDescription());
        job.setCategory(updatedJob.getCategory());
        job.setJobType(updatedJob.getJobType());
        job.setLocation(updatedJob.getLocation());
        job.setSalary(updatedJob.getSalary());
        job.setExperience(updatedJob.getExperience());
        job.setSkillsRequired(updatedJob.getSkillsRequired());
        job.setExpiryDate(updatedJob.getExpiryDate());
        jobService.updateJob(job);
        return "redirect:/employer/dashboard?updated=true";
    }

    // ── Withdraw Application ─────────────────────────────────────
    @PostMapping("/student/withdraw/{applicationId}")
    public String withdrawApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        applicationService.withdrawApplication(applicationId, student);
        return "redirect:/student/applications?withdrawn=true";
    }

    // ── Save / Unsave Job ────────────────────────────────────────
    @PostMapping("/student/save-job/{jobId}")
    public String toggleSaveJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "Referer",
                           defaultValue = "/jobs/browse")
            String referer) {
        User student = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        Job job = jobService.findById(jobId).orElseThrow();
        savedJobService.toggleSaveJob(student, job);
        return "redirect:" + referer;
    }

    // ── Saved Jobs Page ──────────────────────────────────────────
    @GetMapping("/student/saved-jobs")
    public String savedJobsPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User student = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        model.addAttribute("savedJobs",
            savedJobService.getSavedJobs(student));
        return "student/saved-jobs";
    }
}