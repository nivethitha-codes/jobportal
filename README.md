# ⚡ JobSpark — Job Portal Management System

A full-stack job portal connecting **Students**, **Employers**, and **Admins** — built with Java Spring Boot, Thymeleaf, and PostgreSQL. Supports job listings, applications, resume management, role-based dashboards, and an integrated AI chatbot assistant.

🔗 **Live Demo:** https://jobspark-xy7z.onrender.com
📦 **Repository:** https://github.com/nivethitha-codes/jobportal

---

## ✨ Features

- **Role-based access** — separate flows and dashboards for Students, Employers, and Admins
- **Job listings & applications** — post, browse, filter, and apply to jobs
- **Resume management** — upload and attach resumes to applications
- **Email OTP registration** — secure signup verification via email
- **Search & autocomplete** — fast job/skill/location search
- **Job bookmarks** — save listings to revisit later
- **Automated job expiry** — listings auto-expire after a set period
- **AI chatbot assistant** — helps users navigate the platform and find relevant jobs
- **Secure authentication** — Spring Security with BCrypt password hashing

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot (MVC architecture) |
| Templating | Thymeleaf |
| Security | Spring Security, BCrypt |
| Database | PostgreSQL |
| Deployment | Render |

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven
- PostgreSQL (local or hosted)

### Setup

1. Clone the repo
```bash
   git clone https://github.com/nivethitha-codes/jobportal.git
   cd jobportal
```

2. Configure your database and mail credentials in `src/main/resources/application.properties`:
```properties
   spring.datasource.url=jdbc:postgresql://<host>:<port>/<database>
   spring.datasource.username=<your-username>
   spring.datasource.password=<your-password>

   spring.mail.username=<your-email>
   spring.mail.password=<your-app-password>
```

3. Build and run
```bash
   ./mvnw spring-boot:run
```

4. Visit `http://localhost:8080` in your browser.

## 📁 Project Structure
```text
jobportal/
├── src/
│ ├── main/
│ │ ├── java/ # Controllers, Services, Repositories, Models
│ │ └── resources/ # Templates, static assets, application.properties
├── uploads/resumes/ # Uploaded resume storage
└── pom.xml
```


## 🎯 Project Goal

Built as a full-stack academic project mapped to **UN SDG Goal 8 — Decent Work and Economic Growth**, aiming to make job discovery and hiring more accessible for students entering the workforce.

## 👤 Author

**P. Nivethitha**
[LinkedIn](https://linkedin.com/in/nivethitha-tech) · [GitHub](https://github.com/nivethitha-codes)  .
