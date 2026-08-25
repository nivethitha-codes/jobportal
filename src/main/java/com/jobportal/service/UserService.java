package com.jobportal.service;

import com.jobportal.model.Job;
import com.jobportal.model.Role;
import com.jobportal.model.User;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.SavedJobRepository;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail()))
            throw new RuntimeException("Email already registered!");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public String uploadResume(MultipartFile file,
                                Long userId) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath))
            Files.createDirectories(uploadPath);

        String fileName = "resume_" + userId + "_"
                          + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath,
                   StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // ── Change Password ──────────────────────────────────────────
    public boolean changePassword(String email,
                                   String oldPassword,
                                   String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword,
                user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    // ── Delete User — Fixed with FK cleanup ──────────────────────
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() ->
                new RuntimeException("User not found"));

        // Step 1 — Delete student's saved jobs
        savedJobRepository.deleteByStudent(user);

        // Step 2 — Delete student's applications
        applicationRepository.deleteByApplicant(user);

        // Step 3 — Delete employer's jobs + related data
        List<Job> jobs = jobRepository.findByEmployer(user);
        for (Job job : jobs) {
            applicationRepository.deleteByJob(job);
            savedJobRepository.deleteByJob(job);
        }
        jobRepository.deleteAll(jobs);

        // Step 4 — Finally delete the user
        userRepository.delete(user);
    }
}