package com.lalit.authservice.service;

import com.lalit.authservice.DTO.*;
import com.lalit.authservice.entity.AuthUser;
import com.lalit.authservice.entity.RefreshToken;
import com.lalit.authservice.entity.UserRole;
import com.lalit.authservice.execption.*;
import com.lalit.authservice.repository.AuthUserRepository;
import com.lalit.authservice.repository.RefreshTokenRepository;
import com.lalit.authservice.utils.DeviceFingerprintUtil;
import com.lalit.authservice.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthUserRepository authUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    //register the user
    public RegisterResponse register(RegisterRequest request)
    {
        if(authUserRepository.existsByEmail(request.getEmail()))
        {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        AuthUser user = AuthUser.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .role(request.getRole()!=null?  request.getRole(): UserRole.STUDENT)
                .createdAt(LocalDateTime.now())
        .build();
        AuthUser savedUser = authUserRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .isActive(savedUser.getIsActive())
                .createdAt(savedUser.getCreatedAt())
                .message("User registered successfully")
                .build();
    }

    //login user and generate token
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent ) {
        //  Find user by email
        AuthUser user = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new AccountInactiveException("Account is inactive");
        }

        //Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate Jwt access token
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        // Generate device fingerprint
        String deviceFingerprint = DeviceFingerprintUtil.generateFingerprint(ipAddress, userAgent);

        // Check if this device has logged in before
        boolean isKnownDevice = refreshTokenRepository
                .existsByAuthUserIdAndDeviceFingerprint(user.getId(), deviceFingerprint);

        // Save refresh token with device info
        String refreshTokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .authUser(user)
                .expiryDate(LocalDateTime.now().plusDays(30))
                .deviceFingerprint(deviceFingerprint)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isNewDevice(!isKnownDevice)
                .build();

        refreshTokenRepository.save(refreshToken);

        // Send alert email only for new/unknown device
        if (!isKnownDevice) {
            emailService.sendNewDeviceLoginAlert(
                    user.getEmail(),
                    extractNameFromEmail(user.getEmail()),
                    ipAddress,
                    userAgent,
                    LocalDateTime.now()
            );
        }

        // Build and return response
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(userDto)
                .build();
    }

    // Helper
    private String extractNameFromEmail(String email) {
        return email.split("@")[0];
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        // Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));


        // Check if token is expired
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token expired");
        }

        // Generate new access token
        String accessToken = jwtTokenProvider.generateAccessToken(refreshToken.getAuthUser());

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();
    }

    //logout from the single device
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    //logout from all the device
    public void logoutAllDevices(Long userId) {
        refreshTokenRepository.deleteByAuthUserId(userId);
    }

    public AuthUser getUserById(Long userId) {
        return authUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    //update the user role
    public void updateUserRole(Long userId, UserRole role) {
        AuthUser user = getUserById(userId);
        user.setRole(role);
        authUserRepository.save(user);
    }


    public void deactivateUser(Long adminId, Long targetUserId) {
        // Fetch admin
        AuthUser admin = authUserRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Requesting user not found with id: " + adminId));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException("Only admins can deactivate user accounts");
        }

        // Prevent admin from deactivating themselves
        if (adminId.equals(targetUserId)) {
            throw new UnauthorizedActionException("Admins cannot deactivate their own account");
        }

        // Fetch the target user
        AuthUser targetUser = getUserById(targetUserId);

        // Check if already deactivated
        if (!targetUser.getIsActive()) {
            throw new IllegalStateException("User account is already deactivated");
        }

        // Deactivate and invalidate all sessions
        targetUser.setIsActive(false);
        authUserRepository.save(targetUser);
        logoutAllDevices(targetUserId);
    }


    // only admin can activate user
    public void activateUser(Long adminId, Long targetUserId) {
        //  fetch admin
        AuthUser admin = authUserRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Requesting user not found with id: " + adminId));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException("Only admins can activate user accounts");
        }

        // Fetch target user
        AuthUser targetUser = getUserById(targetUserId);

        // Guard against redundant activation
        if (targetUser.getIsActive()) {
            throw new IllegalStateException("User account is already active");
        }

        // Reactivate the account
        targetUser.setIsActive(true);
        authUserRepository.save(targetUser);
    }

    // get all users by admin
    public Page<UserDtoWithIsActive> getAllUsers(Long adminId, Pageable pageable) {
        // fetch admin
        AuthUser admin = authUserRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Requesting user not found with id: " + adminId));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException("Only admins can view all users");
        }

        // db-level paginated fetch — no in-memory filtering, no full table load
        return authUserRepository.findByRoleNot(UserRole.ADMIN, pageable)
                .map(user -> UserDtoWithIsActive.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .isActive(user.getIsActive())
                        .build());
    }



    //  Cleanup expired tokens
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(LocalDateTime.now());
    }
}
