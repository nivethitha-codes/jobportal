package com.jobportal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    // Store OTPs in memory — email → otp
    private final Map<String, String> otpStore =
        new ConcurrentHashMap<>();

    // Store OTP expiry time — email → expiry time
    private final Map<String, Long> otpExpiry =
        new ConcurrentHashMap<>();

    // OTP valid for 5 minutes
    private static final long OTP_VALIDITY_MS = 5 * 60 * 1000;

    // Generate 6 digit OTP
    public String generateOtp(String email) {
        String otp = String.format("%06d",
            new Random().nextInt(999999));
        otpStore.put(email, otp);
        otpExpiry.put(email, System.currentTimeMillis()
            + OTP_VALIDITY_MS);
        log.info("OTP generated for {}: {}", email, otp);
        return otp;
    }

    // Verify OTP
    public boolean verifyOtp(String email, String otp) {
        String stored  = otpStore.get(email);
        Long   expiry  = otpExpiry.get(email);

        if (stored == null || expiry == null) {
            return false;
        }

        // Check if expired
        if (System.currentTimeMillis() > expiry) {
            otpStore.remove(email);
            otpExpiry.remove(email);
            return false;
        }

        // Check if OTP matches
        if (stored.equals(otp)) {
            otpStore.remove(email);
            otpExpiry.remove(email);
            return true;
        }

        return false;
    }

    // Check if OTP was sent
    public boolean hasOtp(String email) {
        return otpStore.containsKey(email);
    }
}