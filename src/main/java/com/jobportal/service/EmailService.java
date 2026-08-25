package com.jobportal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendApplicationStatusEmail(String toEmail,
                                           String studentName,
                                           String jobTitle,
                                           String companyName,
                                           String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("JobSpark — Application Update: " + jobTitle);
            helper.setText(buildEmailTemplate(
                studentName, jobTitle, companyName, status), true);

            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}",
                toEmail, e.getMessage());
        }
    }

    private String buildEmailTemplate(String studentName,
                                      String jobTitle,
                                      String companyName,
                                      String status) {

        String statusColor = status.equals("SHORTLISTED")
            ? "#10B981" : "#EF4444";
        String statusIcon  = status.equals("SHORTLISTED")
            ? "🎉" : "😔";
        String statusText  = status.equals("SHORTLISTED")
            ? "Shortlisted" : "Not Selected";
        String statusMsg   = status.equals("SHORTLISTED")
            ? "Congratulations! You have been shortlisted for this position. "
            + "The employer will contact you soon for the next steps. "
            + "Keep up the great work!"
            : "Thank you for your interest in this position. "
            + "Unfortunately your application was not selected this time. "
            + "Don't give up — keep applying and the right opportunity will come!";

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
            </head>
            <body style="margin:0; padding:0; font-family:Arial,sans-serif;
                         background:#F0EEFF;">
              <div style="max-width:600px; margin:40px auto; background:#ffffff;
                          border-radius:20px; overflow:hidden;
                          box-shadow:0 8px 32px rgba(124,58,237,0.15);">

                <!-- Header -->
                <div style="background:linear-gradient(135deg,#0A0A1A,#1A1A3E);
                            padding:40px; text-align:center;">
                  <div style="font-size:2rem; font-weight:900; color:#ffffff;
                              letter-spacing:-1px;">
                    Job<span style="color:#7C3AED;">Spark</span>
                  </div>
                  <p style="color:rgba(255,255,255,0.5); margin-top:8px;
                             font-size:0.875rem;">
                    Application Status Update
                  </p>
                </div>

                <!-- Body -->
                <div style="padding:40px;">
                  <h2 style="font-size:1.5rem; font-weight:900;
                              color:#1A1A2E; margin-bottom:8px;">
                    Hey %s! %s
                  </h2>
                  <p style="color:#6B7280; line-height:1.7; margin-bottom:24px;">
                    Your application status has been updated.
                    Here are the details:
                  </p>

                  <!-- Status Badge -->
                  <div style="text-align:center; margin-bottom:24px;">
                    <span style="display:inline-block;
                                 background:%s; color:#ffffff;
                                 padding:10px 28px; border-radius:30px;
                                 font-weight:800; font-size:1rem;
                                 letter-spacing:0.5px;">
                      %s %s
                    </span>
                  </div>

                  <!-- Job Card -->
                  <div style="background:#F0EEFF; border-radius:14px;
                              padding:24px; margin-bottom:24px;
                              border-left:5px solid #7C3AED;">
                    <p style="font-weight:800; color:#1A1A2E;
                               margin:0 0 8px; font-size:1.1rem;">
                      💼 %s
                    </p>
                    <p style="color:#6B7280; margin:0; font-size:0.9rem;">
                      🏢 %s
                    </p>
                  </div>

                  <!-- Message -->
                  <p style="color:#6B7280; line-height:1.8;
                             font-size:0.9rem; margin-bottom:32px;">
                    %s
                  </p>

                  <!-- Button -->
                  <div style="text-align:center;">
                    <a href="http://localhost:8080/student/applications"
                       style="display:inline-block;
                              background:linear-gradient(135deg,#7C3AED,#6D28D9);
                              color:#ffffff; padding:14px 32px;
                              border-radius:12px; text-decoration:none;
                              font-weight:700; font-size:0.95rem;">
                      View My Applications →
                    </a>
                  </div>
                </div>

                <!-- Footer -->
                <div style="background:#F0EEFF; padding:24px 40px;
                            text-align:center;">
                  <p style="color:#6B7280; font-size:0.78rem; margin:0;">
                    © 2026 JobSpark. All rights reserved.
                  </p>
                  <p style="color:#9CA3AF; font-size:0.75rem; margin-top:4px;">
                    This email was sent because you applied for a job on JobSpark.
                  </p>
                </div>

              </div>
            </body>
            </html>
            """.formatted(
                studentName, statusIcon,
                statusColor, statusIcon, statusText,
                jobTitle, companyName,
                statusMsg
            );
    }
 // ── Send OTP Email ───────────────────────────────────────────
    @Async
    public void sendOtpEmail(String toEmail,
                              String userName,
                              String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("JobSpark — Your OTP Verification Code");
            helper.setText(buildOtpTemplate(userName, otp), true);

            mailSender.send(message);
            log.info("✅ OTP email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send OTP to {}: {}",
                toEmail, e.getMessage());
        }
    }

    private String buildOtpTemplate(String userName, String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0; padding:0;
                         font-family:Arial,sans-serif;
                         background:#F5F3FF;">
              <div style="max-width:500px; margin:40px auto;
                          background:#ffffff; border-radius:20px;
                          overflow:hidden;
                          box-shadow:0 8px 32px rgba(109,40,217,0.15);">

                <!-- Header -->
                <div style="background:linear-gradient(135deg,#1E1030,#2D1B69);
                            padding:40px; text-align:center;">
                  <div style="font-size:2rem; font-weight:900;
                              color:#ffffff; letter-spacing:-1px;">
                    Job<span style="color:#D97706;">Spark</span>
                  </div>
                  <p style="color:rgba(255,255,255,0.5);
                             margin-top:8px; font-size:0.875rem;">
                    Email Verification
                  </p>
                </div>

                <!-- Body -->
                <div style="padding:40px; text-align:center;">
                  <div style="font-size:2.5rem; margin-bottom:1rem;">
                    🔐
                  </div>
                  <h2 style="font-size:1.4rem; font-weight:900;
                              color:#1E1030; margin-bottom:8px;">
                    Hey %s! Verify Your Email
                  </h2>
                  <p style="color:#6B7280; line-height:1.7;
                             margin-bottom:2rem; font-size:0.9rem;">
                    Use the OTP below to complete your
                    JobSpark registration. This code expires
                    in <strong>5 minutes</strong>.
                  </p>

                  <!-- OTP Box -->
                  <div style="background:#F5F3FF;
                              border:2px dashed #D97706;
                              border-radius:16px; padding:24px;
                              margin-bottom:2rem;">
                    <p style="font-size:0.8rem; color:#6B7280;
                               margin-bottom:8px; font-weight:600;
                               letter-spacing:1px;
                               text-transform:uppercase;">
                      Your OTP Code
                    </p>
                    <div style="font-size:3rem; font-weight:900;
                                color:#2D1B69; letter-spacing:12px;">
                      %s
                    </div>
                  </div>

                  <p style="color:#9CA3AF; font-size:0.8rem;
                             line-height:1.6;">
                    ⚠️ Never share this OTP with anyone.<br>
                    This code will expire in 5 minutes.
                  </p>
                </div>

                <!-- Footer -->
                <div style="background:#F5F3FF; padding:20px 40px;
                            text-align:center;">
                  <p style="color:#9CA3AF; font-size:0.78rem; margin:0;">
                    © 2026 JobSpark. All rights reserved.
                  </p>
                  <p style="color:#C4B5FD; font-size:0.75rem;
                             margin-top:4px;">
                    If you didn't request this, ignore this email.
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(userName, otp);
    }
}