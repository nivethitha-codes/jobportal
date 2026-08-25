-- See All Registered Users--
USE jobportal_db;

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
FROM users;

-- See All Posted Jobs--
SELECT 
    j.id,
    j.title,
    j.category,
    j.location,
    j.experience,
    j.salary,
    j.job_type,
    j.skills_required,
    j.status,
    j.posted_at,
    u.full_name AS employer_name,
    u.company_name
FROM jobs j
JOIN users u ON j.employer_id = u.id;

-- See All Applications--
SELECT 
    a.id,
    u.full_name AS student_name,
    u.email AS student_email,
    j.title AS job_title,
    c.company_name AS company,
    a.status,
    a.applied_at,
    a.cover_letter
FROM applications a
JOIN users u ON a.applicant_id = u.id
JOIN jobs j ON a.job_id = j.id
JOIN users c ON j.employer_id = c.id;

--  See Applications Per Job--
SELECT 
    j.title AS job_title,
    u.company_name,
    COUNT(a.id) AS total_applicants,
    SUM(CASE WHEN a.status = 'SHORTLISTED' THEN 1 ELSE 0 END) AS shortlisted,
    SUM(CASE WHEN a.status = 'PENDING'     THEN 1 ELSE 0 END) AS pending,
    SUM(CASE WHEN a.status = 'REJECTED'    THEN 1 ELSE 0 END) AS rejected
FROM jobs j
LEFT JOIN applications a ON j.id = a.job_id
JOIN users u ON j.employer_id = u.id
GROUP BY j.id, j.title, u.company_name;

-- See Students Only--
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
WHERE role = 'STUDENT';

-- See Employers Only--
SELECT 
    id,
    full_name,
    email,
    phone,
    company_name,
    location,
    created_at
FROM users
WHERE role = 'EMPLOYER';


INSERT INTO users 
    (full_name, email, password, role, created_at) 
VALUES (
    'Admin User',
    'admin@jobspark.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'ADMIN',
    NOW()
);


-- Full Database Overview--
SELECT 'Users'        AS table_name, COUNT(*) AS total FROM users
UNION ALL
SELECT 'Jobs'         AS table_name, COUNT(*) AS total FROM jobs
UNION ALL
SELECT 'Applications' AS table_name, COUNT(*) AS total FROM applications;


