package com.example.user_onboarding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendVerificationEmail(String email) {
        // In a real application, this would use an email service provider
        logger.info("Sending verification email to: {}", email);
    }

    public void sendWelcomeEmail(String email) {
        // In a real application, this would use an email service provider
        logger.info("Sending welcome email to: {}", email);
    }
}