    package com.lalit.authservice.service;

    import com.lalit.authservice.DTO.*;
    import com.lalit.authservice.entity.AuthUser;
    import com.lalit.authservice.entity.PasswordResetToken;
    import com.lalit.authservice.repository.AuthUserRepository;
    import com.lalit.authservice.repository.PasswordResetTokenRepository;
    import com.lalit.authservice.execption.*;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.UUID;

    @Service
    @RequiredArgsConstructor
    @Transactional
    public class PasswordResetService {

        private final AuthUserRepository authUserRepository;
        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;

        // Token valid for 1 hour
        private static final int TOKEN_VALIDITY_HOURS = 1;

        // User requests password reset and Generates token and sends email
        // This will take mail and send the request to user to reset their mail
        public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
            // Find user by email
            AuthUser user = authUserRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException(
                            "If this email exists in our system, you will receive a password reset link."));

            // Check if user is active
            if (!user.getIsActive()) {
                throw new AccountInactiveException("Account is inactive");
            }

            // Delete any existing unused tokens for this user
            passwordResetTokenRepository.deleteByUserId(user.getId());

            // Generate unique token
            String token = UUID.randomUUID().toString();

            // Create password reset token
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS))
                    .used(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            passwordResetTokenRepository.save(resetToken);

            // Send email with reset link
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    token,
                    extractNameFromEmail(user.getEmail())
            );

            return ForgotPasswordResponse.builder()
                    .message("If this email exists in our system, you will receive a password reset link.")
                    //this maskEmail will convert into hide formate
                    //like lal*****gmail.com
                    .email(maskEmail(request.getEmail()))
                    .build();
        }

         // it validate token and it called when user clicked on link
        // this method help to check was token was valid or not valid
        public TokenValidationResponse validateResetToken(String token) {
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                    .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

            // Check if token is expired
            if (resetToken.isExpired()) {
                throw new TokenExpiredException("Reset token has expired. Please request a new one.");
            }

            // Check if token is already used
            if (resetToken.getUsed()) {
                throw new InvalidTokenException("This reset token has already been used.");
            }

            // by enable this valid flag we can check was user as alrady one token or not
            return TokenValidationResponse.builder()
                    .valid(true)
                    .email(resetToken.getUser().getEmail())
                    .message("Token is valid. You can now reset your password.")
                    .build();
        }

        // reset password with token
        public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new PasswordMismatchException("Passwords do not match");
            }

            // Find token
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                    .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

            // Check if token is expired
            if (resetToken.isExpired()) {
                throw new TokenExpiredException("Reset token has expired. Please request a new one.");
            }

            // Check if token is already used
            if (resetToken.getUsed()) {
                throw new InvalidTokenException("This reset token has already been used.");
            }

            // Get user
            AuthUser user = resetToken.getUser();

            // Check: Is the new password the same as the current old password?
            if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
                throw new SamePasswordException("New password cannot be the same as your current password.");
            }

            // Update password
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            authUserRepository.save(user);

            // Mark token as used
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);

            // Send confirmation email
            emailService.sendPasswordChangedConfirmationEmail(
                    user.getEmail(),
                    extractNameFromEmail(user.getEmail())
            );

            return ResetPasswordResponse.builder()
                    .success(true)
                    .message("Password has been reset successfully. You can now login with your new password.")
                    .email(user.getEmail())
                    .build();
        }

        // cleanup expired token
        public void cleanupExpiredTokens() {
            passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        }

        // Helper methods
        private String extractNameFromEmail(String email) {
            return email.split("@")[0];
        }


    //This method is a security utility used to "hide" or mask sensitive information.
    // It turns a full email address like lalit.developer@gmail.com into something like la***@gmail.com.
        private String maskEmail(String email) {
            String[] parts = email.split("@");
            if (parts.length != 2) return email;

            String username = parts[0];
            String domain = parts[1];

            if (username.length() <= 2) {
                return username.charAt(0) + "***@" + domain;
            }

            return username.substring(0, 2) + "***@" + domain;
        }
    }
