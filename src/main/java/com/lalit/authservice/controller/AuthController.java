package com.lalit.authservice.controller;



import com.lalit.authservice.DTO.*;
import com.lalit.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class AuthController {

    private final AuthService authService;

    // Register User
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request) {

        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Login User
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Client-IP",    defaultValue = "unknown") String clientIp,
            @RequestHeader(value = "X-Device-Agent", defaultValue = "Unknown") String userAgent) {

        log.info("Login attempt from IP: {}, Device: {}", clientIp, userAgent);

        LoginResponse response = authService.login(request, clientIp, userAgent);
        return ResponseEntity.ok(response);
    }

    // Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    // Logout (single device)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestBody RefreshTokenRequest request) {

        authService.logout(request.getRefreshToken());
        log.info("Refresh Toke  received: {}",request.getRefreshToken());
        return ResponseEntity.ok(
                new MessageResponse("Logged out successfully")
        );
    }

    // Logout all devices
    @PostMapping("/logout-all/{userId}")
    public ResponseEntity<?> logoutAllDevices(
            @PathVariable Long userId) {

        authService.logoutAllDevices(userId);
        return ResponseEntity.ok(
                new MessageResponse("Logged out from all devices")
        );
    }



    //  Activate a deactivated user account (Admin only)
    @PatchMapping("/admin/activate/{userId}")
    public ResponseEntity<?> activateUser(
            @PathVariable Long userId,
            @RequestParam Long adminId) {

        log.info("Activate request by adminId={} for targetUserId={}", adminId, userId);
        authService.activateUser(adminId, userId);
        return ResponseEntity.ok(new MessageResponse("User account activated successfully"));
    }



    @PatchMapping("/admin/deactivate/{userId}")
    public ResponseEntity<?> deactivateUser(
            @PathVariable Long userId,
            @RequestParam Long adminId) {

        log.info("Deactivate request by adminId={} for targetUserId={}", adminId, userId);
        authService.deactivateUser(adminId, userId);
        return ResponseEntity.ok(new MessageResponse("User account deactivated successfully"));
    }


    @GetMapping("/admin/users")
    public ResponseEntity<Page<UserDtoWithIsActive>> getAllUsers(
            @RequestParam Long adminId,
            Pageable pageable) {

        log.info("Get all users request by adminId={}, page={}, size={}",
                adminId, pageable.getPageNumber(), pageable.getPageSize());
        Page<UserDtoWithIsActive> users = authService.getAllUsers(adminId, pageable);
        return ResponseEntity.ok(users);
    }

    //Delete all  expired token
    @DeleteMapping("/admin/tokens/cleanup")
    public ResponseEntity<?> cleanupExpiredToken(){

        authService.cleanupExpiredTokens();
        return ResponseEntity.ok(
                new MessageResponse("Expired tokens cleaned successfully")
        );
    }
}
