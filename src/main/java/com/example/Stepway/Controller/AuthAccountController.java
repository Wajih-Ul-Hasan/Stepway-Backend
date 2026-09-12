package com.example.Stepway.Controller;

import com.example.Stepway.Service.impl.AccountRecoveryService;
import com.example.Stepway.dto.AuthMessageResponse;
import com.example.Stepway.dto.EmailRequest;
import com.example.Stepway.dto.ResetPasswordRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthAccountController {
    private final AccountRecoveryService accountRecoveryService;

    public AuthAccountController(AccountRecoveryService accountRecoveryService) {
        this.accountRecoveryService = accountRecoveryService;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<AuthMessageResponse> verifyEmail(@RequestParam String token) {
        accountRecoveryService.verifyEmail(token);
        return ResponseEntity.ok(new AuthMessageResponse("Email verified successfully"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<AuthMessageResponse> resendVerification(@Valid @RequestBody EmailRequest request) {
        accountRecoveryService.requestVerification(request.getEmail());
        return ResponseEntity.ok(new AuthMessageResponse("If the account needs verification, an email has been sent"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthMessageResponse> forgotPassword(@Valid @RequestBody EmailRequest request) {
        accountRecoveryService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(new AuthMessageResponse("If that email exists, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthMessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        accountRecoveryService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new AuthMessageResponse("Password reset successfully"));
    }
}
