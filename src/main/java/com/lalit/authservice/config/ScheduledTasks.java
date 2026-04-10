package com.lalit.authservice.config;

import com.lalit.authservice.service.AuthService;
import com.lalit.authservice.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final PasswordResetService passwordResetService;
    private  final AuthService authService;


    // This method Runs every day at 2 AM
    // Cleans expired password reset tokens (1 hour old)
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredPasswordResetTokens() {
        log.info("Scheduler → Cleaning expired password reset tokens...");
        passwordResetService.cleanupExpiredTokens();
        log.info("Scheduler → Password reset token cleanup done ✅");
    }


    //Runs every day at 3 AM
    //Cleans expired refresh tokens (30 days old)
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredRefreshTokens() {
        log.info("Scheduler → Cleaning expired refresh tokens...");
        authService.cleanupExpiredTokens();
        log.info("Scheduler → Refresh token cleanup done ✅");
    }
}
