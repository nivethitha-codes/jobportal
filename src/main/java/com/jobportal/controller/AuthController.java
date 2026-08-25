package com.jobportal.controller;

import com.jobportal.model.User;
import com.jobportal.service.EmailService;
import com.jobportal.service.JobService;
import com.jobportal.service.OtpService;
import com.jobportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JobService jobService;
    private final OtpService otpService;
    private final EmailService emailService;

    // ── Home Page ────────────────────────────────────────────────
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("latestJobs",
            jobService.getAllActiveJobs()
                .stream().limit(6).toList());
        return "home";
    }

    // ── Login Page ───────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // ── Register Page GET ────────────────────────────────────────
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    // ── Step 1: Send OTP ─────────────────────────────────────────
    @PostMapping("/register/send-otp")
    public String sendOtp(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String role,
            @RequestParam(required = false) String companyName,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("user", new User());
            model.addAttribute("passwordError",
                "Passwords do not match!");
            return "auth/register";
        }

        // Validate password length
        if (password.length() < 6) {
            model.addAttribute("user", new User());
            model.addAttribute("passwordError",
                "Password must be at least 6 characters!");
            return "auth/register";
        }

        // Check email already registered
        if (userService.findByEmail(email).isPresent()) {
            model.addAttribute("user", new User());
            model.addAttribute("emailError",
                "Email already registered!");
            return "auth/register";
        }

        // Generate and send OTP
        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, fullName, otp);

        // Pass data to OTP verification page
        redirectAttributes.addFlashAttribute("fullName",    fullName);
        redirectAttributes.addFlashAttribute("email",       email);
        redirectAttributes.addFlashAttribute("password",    password);
        redirectAttributes.addFlashAttribute("role",        role);
        redirectAttributes.addFlashAttribute("companyName", companyName);
        redirectAttributes.addFlashAttribute("otpSent",     true);

        return "redirect:/register/verify-otp";
    }

    // ── OTP Verification Page GET ────────────────────────────────
    @GetMapping("/register/verify-otp")
    public String verifyOtpPage(Model model) {
        // If no OTP data in session redirect back
        if (!model.containsAttribute("email")) {
            return "redirect:/register";
        }
        return "auth/verify-otp";
    }

    // ── Step 2: Verify OTP & Register ────────────────────────────
    @PostMapping("/register/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String fullName,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam(required = false) String companyName,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Verify OTP
        if (!otpService.verifyOtp(email, otp)) {
            model.addAttribute("email",       email);
            model.addAttribute("fullName",    fullName);
            model.addAttribute("password",    password);
            model.addAttribute("role",        role);
            model.addAttribute("companyName", companyName);
            model.addAttribute("otpError",
                "Invalid or expired OTP! Please try again.");
            return "auth/verify-otp";
        }

        // OTP verified — register the user
        try {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(com.jobportal.model.Role
                .valueOf(role));
            if (companyName != null && !companyName.isEmpty()) {
                user.setCompanyName(companyName);
            }
            userService.registerUser(user);
            return "redirect:/login?registered=true";

        } catch (RuntimeException e) {
            model.addAttribute("otpError", e.getMessage());
            return "auth/verify-otp";
        }
    }

    // ── Resend OTP ───────────────────────────────────────────────
    @PostMapping("/register/resend-otp")
    public String resendOtp(
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam(required = false) String companyName,
            RedirectAttributes redirectAttributes) {

        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, fullName, otp);

        redirectAttributes.addFlashAttribute("fullName",    fullName);
        redirectAttributes.addFlashAttribute("email",       email);
        redirectAttributes.addFlashAttribute("password",    password);
        redirectAttributes.addFlashAttribute("role",        role);
        redirectAttributes.addFlashAttribute("companyName", companyName);
        redirectAttributes.addFlashAttribute("otpSent",     true);
        redirectAttributes.addFlashAttribute("resent",      true);

        return "redirect:/register/verify-otp";
    }
}