package com.jobportal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Job title is required")
    @Size(max = 150)
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank
    private String skillsRequired;

    private String salary;
    private String location;
    private String category;
    private String experience;
    private String jobType;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(updatable = false)
    private LocalDateTime postedAt;

    // ── NEW: Expiry Date ─────────────────────────────────────
    private LocalDate expiryDate;

    @PrePersist
    protected void onCreate() {
        postedAt = LocalDateTime.now();
        status   = JobStatus.ACTIVE;
        // Default expiry = 30 days from today
        if (expiryDate == null) {
            expiryDate = LocalDate.now().plusDays(30);
        }
    }

    // Check if job is expired
    public boolean isExpired() {
        return expiryDate != null &&
               LocalDate.now().isAfter(expiryDate);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @OneToMany(mappedBy = "job",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    private List<Application> applications;
}