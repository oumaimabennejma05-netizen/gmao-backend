package com.gmao.controller;

import com.gmao.dto.ApiResponse;
import com.gmao.dto.LoginRequest;
import com.gmao.dto.LoginResponse;
import com.gmao.entity.User;
import com.gmao.repository.UserRepository;
import com.gmao.service.AuthService;
import com.gmao.service.TwoFactorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TwoFactorService twoFactorService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService,
                          TwoFactorService twoFactorService,
                          UserRepository userRepository) {
        this.authService = authService;
        this.twoFactorService = twoFactorService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    /**
     * Step 1 of 2FA setup: generate secret + QR code URI.
     * The user must confirm with a code before 2FA is actually enabled.
     */
    @PostMapping("/2fa/setup")
    public ResponseEntity<ApiResponse<Map<String, String>>> setup2FA(
            @AuthenticationPrincipal User currentUser) {
        String secret = twoFactorService.generateSecret();
        String qrUri = twoFactorService.buildQrUri(currentUser.getEmail(), secret);

        // Store secret temporarily (not activated until verified)
        currentUser.setTwoFactorSecret(secret);
        currentUser.setTwoFactorEnabled(false);
        userRepository.save(currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Scan the QR code with your authenticator app, then verify",
                Map.of(
                        "secret", secret,
                        "qrUri", qrUri,
                        "accountName", currentUser.getEmail()
                )));
    }

    /**
     * Step 2 of 2FA setup: verify the 6-digit code and activate 2FA.
     */
    @PostMapping("/2fa/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verify2FA(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {
        String code = body.getOrDefault("code", "");

        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        if (user.getTwoFactorSecret() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("2FA not set up. Call /api/auth/2fa/setup first."));
        }

        boolean valid = twoFactorService.verifyCode(user.getTwoFactorSecret(), code);
        if (!valid) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid 2FA code. Please try again."));
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("Two-Factor Authentication enabled successfully",
                Map.of("twoFactorEnabled", true)));
    }

    /**
     * Disable 2FA — requires a valid code for confirmation.
     */
    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse<Map<String, Object>>> disable2FA(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {
        String code = body.getOrDefault("code", "");

        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        if (!user.isTwoFactorEnabled()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("2FA is not currently enabled."));
        }

        boolean valid = twoFactorService.verifyCode(user.getTwoFactorSecret(), code);
        if (!valid) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid 2FA code."));
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("Two-Factor Authentication disabled",
                Map.of("twoFactorEnabled", false)));
    }
}