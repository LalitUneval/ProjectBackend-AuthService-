package com.lalit.authservice.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.*;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Core method to send email via SendGrid HTTP API
    private void sendEmail(String toEmail, String subject, String body) throws IOException {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        if (response.getStatusCode() == 202) {
            logger.info("Email sent successfully to: {}", toEmail);
        } else {
            logger.error("SendGrid error {} : {}", response.getStatusCode(), response.getBody());
            throw new RuntimeException("Failed to send email: " + response.getBody());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token, String userName) {
        try {
            String resetLink = frontendUrl + token;
            String subject = "Password Reset Request - Migration Hub";
            String body = buildEmailBody(userName, resetLink);
            sendEmail(toEmail, subject, body);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    @Async
    public void sendPasswordChangedConfirmationEmail(String toEmail, String userName) {
        try {
            String subject = "Password Changed Successfully - Migration Hub";
            String body = String.format("""
                Hello %s,
                
                Your password has been successfully changed.
                
                If you didn't make this change, please contact support immediately at support@migrationhub.com
                
                You can now login at: %s/login
                
                Best regards,
                Migration Hub Team
                """, userName, frontendUrl);

            sendEmail(toEmail, subject, body);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to: {}", toEmail, e);
            // Don't throw - password already changed
        }
    }

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
                If this wasn't you, please change your password immediately.
                
                Stay safe,
                Migration Hub Team
                """, userName, loginTime, ipAddress, userAgent);

            sendEmail(toEmail, subject, body);
        } catch (Exception e) {
            logger.error("Failed to send new device alert to: {}", toEmail, e);
        }
    }

    private String buildEmailBody(String userName, String resetLink) {
        return String.format("""
            Hello %s,
            
            We received a request to reset your password for your Migration Hub account.
            
            Click the link below to reset your password:
            %s
            
            This link will expire in 1 hour.
            
            If you didn't request this, please ignore this email.
            
            Best regards,
            Migration Hub Team
            """, userName, resetLink);
    }
}