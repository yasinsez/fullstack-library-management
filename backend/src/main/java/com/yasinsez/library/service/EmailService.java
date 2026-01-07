package com.yasinsez.library.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

@ApplicationScoped
public class EmailService {
    private static final Logger logger = Logger.getLogger(EmailService.class.getName());
    private final String fromEmail = "noreply@yasinsez.com";

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        String subject = "Verify Your Library Account";
        String content = "Please click the link below to verify your account:\n" +
                "http://library.example.com/verify?token=" + verificationToken;

        sendEmail(toEmail, subject, content);
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String subject = "Reset Your Library Account Password";
        String content = "Please click the link below to reset your password:\n" +
                "http://library.example.com/reset-password?token=" + resetToken;

        sendEmail(toEmail, subject, content);
    }

    public void sendSecurityNotification(String toEmail, String message) {
        String subject = "Security Notification - Library Account";
        sendEmail(toEmail, subject, message);
    }

    public void sendEmail(String toEmail, String subject, String content) {
        // In a real implementation, this would connect to an email service
        // For now, just log the email
        logger.info("Sending email to: " + toEmail);
        logger.info("Subject: " + subject);
        logger.info("Content: " + content);
    }
}
