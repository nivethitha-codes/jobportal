package com.jobportal.controller;

import com.jobportal.model.*;
import com.jobportal.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // ── Student Dashboard ────────────────────────────────────────
    @GetMapping("/student/dashboard")
    public String studentDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User student = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", student);
        model.addAttribute("applications",
            applicationService.getApplicationsByStudent(student));
        model.addAttribute("recentJobs",
            jobService.getAllActiveJobs().stream().limit(6).toList());
        return "student/dashboard";
    }

    // ── Student Applications ─────────────────────────────────────
    @GetMapping("/student/applications")
    public String myApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User student = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        model.addAttribute("applications",
            applicationService.getApplicationsByStudent(student));
        return "student/applications";
    }

    // ── Student Resume Upload ────────────────────────────────────
    @PostMapping("/student/upload-resume")
    public String uploadResume(
            @RequestParam MultipartFile resume,
            @AuthenticationPrincipal UserDetails userDetails)
            throws Exception {
        User student = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        String fileName = userService.uploadResume(resume, student.getId());
        student.setResumePath(fileName);
        userService.save(student);
        return "redirect:/student/dashboard?resumeUploaded=true";
    }

    // ── Student Profile GET ──────────────────────────────────────
    @GetMapping("/student/profile")
    public String studentProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User student = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", student);
        return "student/profile";
    }

    // ── Student Profile POST ─────────────────────────────────────
    @PostMapping("/student/profile")
    public String updateStudentProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String location,
            @RequestParam String skills) {
        User student = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        student.setFullName(fullName);
        student.setPhone(phone);
        student.setLocation(location);
        student.setSkills(skills);
        userService.save(student);
        return "redirect:/student/profile?updated=true";
    }

    // ── Student Change Password GET ──────────────────────────────
    @GetMapping("/student/change-password")
    public String studentChangePasswordPage() {
        return "student/change-password";
    }

    // ── Student Change Password POST ─────────────────────────────
    @PostMapping("/student/change-password")
    public String studentChangePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match!");
            return "student/change-password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("error",
                "Password must be at least 6 characters!");
            return "student/change-password";
        }
        boolean changed = userService.changePassword(
            userDetails.getUsername(), oldPassword, newPassword);
        if (!changed) {
            model.addAttribute("error", "Old password is incorrect!");
            return "student/change-password";
        }
        return "redirect:/student/change-password?success=true";
    }

    // ── Employer Dashboard ───────────────────────────────────────
    @GetMapping("/employer/dashboard")
    public String employerDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User employer = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        List<Job> jobs = jobService.getJobsByEmployer(employer);
        int totalApplicants = jobs.stream()
            .mapToInt(j -> j.getApplications().size())
            .sum();
        model.addAttribute("user", employer);
        model.addAttribute("jobs", jobs);
        model.addAttribute("totalApplicants", totalApplicants);
        return "employer/dashboard";
    }

    // ── Employer Profile GET ─────────────────────────────────────
    @GetMapping("/employer/profile")
    public String employerProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User employer = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", employer);
        return "employer/profile";
    }

    // ── Employer Profile POST ────────────────────────────────────
    @PostMapping("/employer/profile")
    public String updateEmployerProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String location,
            @RequestParam String companyName) {
        User employer = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();
        employer.setFullName(fullName);
        employer.setPhone(phone);
        employer.setLocation(location);
        employer.setCompanyName(companyName);
        userService.save(employer);
        return "redirect:/employer/profile?updated=true";
    }

    // ── Employer Change Password GET ─────────────────────────────
    @GetMapping("/employer/change-password")
    public String employerChangePasswordPage() {
        return "employer/change-password";
    }

    // ── Employer Change Password POST ────────────────────────────
    @PostMapping("/employer/change-password")
    public String employerChangePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match!");
            return "employer/change-password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("error",
                "Password must be at least 6 characters!");
            return "employer/change-password";
        }
        boolean changed = userService.changePassword(
            userDetails.getUsername(), oldPassword, newPassword);
        if (!changed) {
            model.addAttribute("error", "Old password is incorrect!");
            return "employer/change-password";
        }
        return "redirect:/employer/change-password?success=true";
    }

    // ── Resume Download ──────────────────────────────────────────
    @GetMapping("/employer/download-resume/{fileName}")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"")
                .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── Admin Dashboard ──────────────────────────────────────────
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {

        List<User> allUsers       = userService.getAllUsers();
        List<User> students       = userService.getUsersByRole(Role.STUDENT);
        List<User> employers      = userService.getUsersByRole(Role.EMPLOYER);
        List<Job> allJobs         = jobService.getAllJobs();
        List<Job> activeJobs      = jobService.getAllActiveJobs();
        List<Application> allApplications =
            applicationService.getAllApplications();

        long shortlisted = allApplications.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED)
            .count();
        long pending = allApplications.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.PENDING)
            .count();
        long rejected = allApplications.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.REJECTED)
            .count();

        model.addAttribute("totalUsers",        allUsers.size());
        model.addAttribute("totalStudents",     students.size());
        model.addAttribute("totalEmployers",    employers.size());
        model.addAttribute("totalJobs",         allJobs.size());
        model.addAttribute("totalActiveJobs",   activeJobs.size());
        model.addAttribute("totalApplications", allApplications.size());
        model.addAttribute("totalShortlisted",  shortlisted);
        model.addAttribute("totalPending",      pending);
        model.addAttribute("totalRejected",     rejected);
        model.addAttribute("allUsers",          allUsers);
        model.addAttribute("allJobs",           allJobs);
        model.addAttribute("allApplications",   allApplications);
        model.addAttribute("recentStudents",
            students.stream().limit(5).toList());
        model.addAttribute("recentEmployers",
            employers.stream().limit(5).toList());

        return "admin/dashboard";
    }

    // ── Admin Delete User ────────────────────────────────────────
    @PostMapping("/admin/delete-user/{id}")
    public String deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentAdmin = userService.findByEmail(
            userDetails.getUsername()).orElseThrow();

        if (currentAdmin.getId().equals(id)) {
            return "redirect:/admin/dashboard?selfDelete=true";
        }

        userService.getAllUsers().stream()
            .filter(u -> u.getId().equals(id))
            .findFirst()
            .ifPresent(u -> {
                if (u.getRole() != Role.ADMIN) {
                    userService.deleteUser(id);
                }
            });

        return "redirect:/admin/dashboard?userDeleted=true";
    }
}