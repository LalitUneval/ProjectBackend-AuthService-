package com.lalit.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200/reset-password?token=}")
    private String frontendUrl;


    // send password reset method
    // when the user enter the mail then this method was used to send it destination of mail
    @Async
    public void sendPasswordResetEmail(String toEmail, String token, String userName) {
        try {

            String resetLink=frontendUrl+token;

            String subject = "Password Reset Request - Migration Hub";

            String body = buildEmailBody(userName, resetLink, token);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            logger.info("Password reset email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    // build email body content
    // In this %s %s %s was reserved seat and at the end we define the varible which we kept at that place
    private String buildEmailBody(String userName, String resetLink, String token) {
        return String.format("""
            Hello %s,
            
            We received a request to reset your password for your Migration Hub account.
            
            Click the link below to reset your password:
            %s
            
            Or copy and paste this link into your browser:
            %s
            
            This link will expire in 1 hour.
            
            If you didn't request a password reset, please ignore this email or contact support if you have concerns.
            
            Best regards,
            Migration Hub Team
            
            ---
            This is an automated email. Please do not reply to this message.
            """,
                userName,
                resetLink,
                resetLink
        );
    }

    // send password changed confirmation mail
    @Async
    public void sendPasswordChangedConfirmationEmail(String toEmail, String userName) {
        try {
                String subject = "Password Changed Successfully - Migration Hub";

            String body = String.format("""
                Hello %s,
                
                Your password has been successfully changed.
                
                If you didn't make this change, please contact our support team immediately at support@migrationhub.com
                
                You can now login with your new password at: %s/login
                
                Best regards,
                Migration Hub Team
                """,
                    userName,
                    frontendUrl
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            logger.info("Password change confirmation email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send confirmation email to: {}", toEmail, e);
            // Don't throw exception here, password is already changed
        }
    }


    // send new device login alert
    @Async
    public void sendNewDeviceLoginAlert(String toEmail, String userName,
                                        String ipAddress, String userAgent,
                                        LocalDateTime loginTime) {
        try {
            String subject = "New Device Login Detected - Migration Hub";
            String body = String.format("""
            Hello %s,
            
            We detected a new login to your account.
            
            Details:
            - Time: %s
            - IP Address: %s
            - Device/Browser: %s
            
            If this was you, no action is needed.
            
            If this wasn't you, please:
            1. Change your password immediately
            2. Go to Security Settings and click "Logout All Devices"
            
            Stay safe,
            Migration Hub Team
            """,
                    userName,
                    loginTime.toString(),
                    ipAddress,
                    userAgent
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            logger.info("New device login alert sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send new device alert to: {}", toEmail, e);
        }
    }
}

