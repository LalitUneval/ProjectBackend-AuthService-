package com.lalit.authservice.controller;

import com.lalit.authservice.DTO.*;
import com.lalit.authservice.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;


    //Generate Request Link
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.forgotPassword(request));
    }

    //  Validate Token
    @GetMapping("/validate-reset-token")
    public ResponseEntity<TokenValidationResponse> validateResetToken(@RequestParam String token) {
        return ResponseEntity.ok(passwordResetService.validateResetToken(token));
    }


     //Actual Reset
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) { // MUST HAVE @RequestBody

        ResetPasswordResponse response = passwordResetService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}