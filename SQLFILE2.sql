-- ═══════════════════════════════════════════════
-- JOBSPARK DATABASE — COMPLETE SQL SCRIPT
-- ═══════════════════════════════════════════════

-- Step 1: Use the database
USE jobportal_db;

-- ═══════════════════════════════════════════════
-- VIEW ALL TABLES
-- ═══════════════════════════════════════════════
SHOW TABLES;

-- ═══════════════════════════════════════════════
-- TABLE STRUCTURES
-- ═══════════════════════════════════════════════

-- Users Table Structure
DESCRIBE users;

-- Jobs Table Structure
DESCRIBE jobs;

-- Applications Table Structure
DESCRIBE applications;

-- Saved Jobs Table Structure
DESCRIBE saved_jobs;

-- ═══════════════════════════════════════════════
-- VIEW ALL DATA
-- ═══════════════════════════════════════════════

-- All Users
SELECT
    id,
    full_name,
    email,
    role,
    phone,
    location,
    skills,
    company_name,
    resume_path,
    created_at
FROM users
ORDER BY created_at DESC;

-- All Students Only
SELECT
    id,
    full_name,
    email,
    phone,
    location,
    skills,
    resume_path,
    created_at
FROM users
WHERE role = 'STUDENT'
ORDER BY created_at DESC;

-- All Employers Only
SELECT
    id,
    full_name,
    company_name,
    email,
    phone,
    location,
    created_at
FROM users
WHERE role = 'EMPLOYER'
ORDER BY created_at DESC;

-- All Jobs
SELECT
    j.id,
    j.title,
    j.category,
    j.job_type,
    j.location,
    j.salary,
    j.experience,
    j.status,
    j.expiry_date,
    j.posted_at,
    u.company_name AS employer,
    u.email AS employer_email
FROM jobs j
JOIN users u ON j.employer_id = u.id
ORDER BY j.posted_at DESC;

-- Active Jobs Only
SELECT
    j.id,
    j.title,
    j.category,
    j.job_type,
    j.location,
    j.salary,
    j.experience,
    j.expiry_date,
    u.company_name AS employer
FROM jobs j
JOIN users u ON j.employer_id = u.id
WHERE j.status = 'ACTIVE'
AND (j.expiry_date IS NULL OR j.expiry_date >= CURDATE())
ORDER BY j.posted_at DESC;

-- Expired Jobs
SELECT
    j.id,
    j.title,
    j.category,
    j.expiry_date,
    u.company_name AS employer
FROM jobs j
JOIN users u ON j.employer_id = u.id
WHERE j.expiry_date < CURDATE()
ORDER BY j.expiry_date DESC;

-- All Applications
SELECT
    a.id,
    u.full_name     AS student_name,
    u.email         AS student_email,
    j.title         AS job_title,
    e.company_name  AS company,
    a.status,
    a.applied_at
FROM applications a
JOIN users u  ON a.applicant_id = u.id
JOIN jobs  j  ON a.job_id = j.id
JOIN users e  ON j.employer_id = e.id
ORDER BY a.applied_at DESC;

-- Shortlisted Applications
SELECT
    a.id,
    u.full_name     AS student_name,
    u.email         AS student_email,
    j.title         AS job_title,
    e.company_name  AS company,
    a.applied_at
FROM applications a
JOIN users u  ON a.applicant_id = u.id
JOIN jobs  j  ON a.job_id = j.id
JOIN users e  ON j.employer_id = e.id
WHERE a.status = 'SHORTLISTED'
ORDER BY a.applied_at DESC;

-- Rejected Applications
SELECT
    a.id,
    u.full_name     AS student_name,
    u.email         AS student_email,
    j.title         AS job_title,
    e.company_name  AS company,
    a.applied_at
FROM applications a
JOIN users u  ON a.applicant_id = u.id
JOIN jobs  j  ON a.job_id = j.id
JOIN users e  ON j.employer_id = e.id
WHERE a.status = 'REJECTED'
ORDER BY a.applied_at DESC;

-- Pending Applications
SELECT
    a.id,
    u.full_name     AS student_name,
    u.email         AS student_email,
    j.title         AS job_title,
    e.company_name  AS company,
    a.applied_at
FROM applications a
JOIN users u  ON a.applicant_id = u.id
JOIN jobs  j  ON a.job_id = j.id
JOIN users e  ON j.employer_id = e.id
WHERE a.status = 'PENDING'
ORDER BY a.applied_at DESC;

-- Saved Jobs
SELECT
    s.id,
    u.full_name     AS student_name,
    u.email         AS student_email,
    j.title         AS job_title,
    e.company_name  AS company,
    j.location,
    j.salary,
    s.saved_at
FROM saved_jobs s
JOIN users u  ON s.student_id = u.id
JOIN jobs  j  ON s.job_id = j.id
JOIN users e  ON j.employer_id = e.id
ORDER BY s.saved_at DESC;

-- ═══════════════════════════════════════════════
-- ANALYTICS & STATISTICS
-- ═══════════════════════════════════════════════

-- Platform Overview Stats
SELECT
    (SELECT COUNT(*) FROM users WHERE role = 'STUDENT')    AS total_students,
    (SELECT COUNT(*) FROM users WHERE role = 'EMPLOYER')   AS total_employers,
    (SELECT COUNT(*) FROM jobs)                            AS total_jobs,
    (SELECT COUNT(*) FROM jobs WHERE status = 'ACTIVE')    AS active_jobs,
    (SELECT COUNT(*) FROM applications)                    AS total_applications,
    (SELECT COUNT(*) FROM applications
     WHERE status = 'SHORTLISTED')                         AS shortlisted,
    (SELECT COUNT(*) FROM applications
     WHERE status = 'PENDING')                             AS pending,
    (SELECT COUNT(*) FROM applications
     WHERE status = 'REJECTED')                            AS rejected,
    (SELECT COUNT(*) FROM saved_jobs)                      AS total_saved_jobs;

-- Top 5 Most Applied Jobs
SELECT
    j.title,
    e.company_name  AS company,
    j.location,
    COUNT(a.id)     AS total_applications
FROM jobs j
JOIN users e       ON j.employer_id = e.id
LEFT JOIN applications a ON a.job_id = j.id
GROUP BY j.id, j.title, e.company_name, j.location
ORDER BY total_applications DESC
LIMIT 5;

-- Top 5 Most Active Employers
SELECT
    u.company_name,
    u.email,
    u.location,
    COUNT(j.id)  AS jobs_posted
FROM users u
LEFT JOIN jobs j ON j.employer_id = u.id
WHERE u.role = 'EMPLOYER'
GROUP BY u.id, u.company_name, u.email, u.location
ORDER BY jobs_posted DESC
LIMIT 5;

-- Most Active Students (by applications)
SELECT
    u.full_name,
    u.email,
    u.location,
    COUNT(a.id)  AS total_applications
FROM users u
LEFT JOIN applications a ON a.applicant_id = u.id
WHERE u.role = 'STUDENT'
GROUP BY u.id, u.full_name, u.email, u.location
ORDER BY total_applications DESC
LIMIT 5;

-- Applications Per Job Category
SELECT
    j.category,
    COUNT(a.id)  AS total_applications
FROM applications a
JOIN jobs j ON a.job_id = j.id
GROUP BY j.category
ORDER BY total_applications DESC;

-- Jobs Per Category
SELECT
    category,
    COUNT(*)     AS total_jobs
FROM jobs
GROUP BY category
ORDER BY total_jobs DESC;

-- Applications Status Summary Per Job
SELECT
    j.title,
    e.company_name,
    COUNT(a.id)                                             AS total,
    SUM(CASE WHEN a.status='SHORTLISTED' THEN 1 ELSE 0 END) AS shortlisted,
    SUM(CASE WHEN a.status='PENDING'     THEN 1 ELSE 0 END) AS pending,
    SUM(CASE WHEN a.status='REJECTED'    THEN 1 ELSE 0 END) AS rejected
FROM jobs j
JOIN users e ON j.employer_id = e.id
LEFT JOIN applications a ON a.job_id = j.id
GROUP BY j.id, j.title, e.company_name
ORDER BY total DESC;

-- Students With Resume Uploaded
SELECT
    full_name,
    email,
    phone,
    location,
    skills,
    resume_path
FROM users
WHERE role = 'STUDENT'
AND resume_path IS NOT NULL
ORDER BY full_name;

-- Students Without Resume
SELECT
    full_name,
    email,
    phone,
    location
FROM users
WHERE role = 'STUDENT'
AND resume_path IS NULL
ORDER BY full_name;

-- Recent Activity (Last 7 Days)
SELECT
    'New User'        AS activity_type,
    full_name         AS details,
    role,
    created_at        AS activity_time
FROM users
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)

UNION ALL

SELECT
    'New Job Posted'  AS activity_type,
    title             AS details,
    'EMPLOYER'        AS role,
    posted_at         AS activity_time
FROM jobs
WHERE posted_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)

UNION ALL

SELECT
    'New Application' AS activity_type,
    CONCAT('App #', id) AS details,
    'STUDENT'         AS role,
    applied_at        AS activity_time
FROM applications
WHERE applied_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)

ORDER BY activity_time DESC;

-- ═══════════════════════════════════════════════
-- ADMIN QUERIES
-- ═══════════════════════════════════════════════

-- Check Admin Account
SELECT
    id,
    full_name,
    email,
    role,
    created_at
FROM users
WHERE role = 'ADMIN';

-- Full Application Details with Cover Letter
SELECT
    a.id,
    u.full_name     AS student_name,
    u.email         AS student_email,
    u.phone         AS student_phone,
    u.skills        AS student_skills,
    j.title         AS job_title,
    e.company_name  AS company,
    j.location,
    j.salary,
    a.cover_letter,
    a.status,
    a.applied_at
FROM applications a
JOIN users u  ON a.applicant_id = u.id
JOIN jobs  j  ON a.job_id = j.id
JOIN users e  ON j.employer_id = e.id
ORDER BY a.applied_at DESC;