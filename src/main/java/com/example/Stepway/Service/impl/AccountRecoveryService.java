package com.example.Stepway.Service.impl;

import com.example.Stepway.Domain.AccountToken;
import com.example.Stepway.Domain.User;
import com.example.Stepway.Repository.AccountTokenRepository;
import com.example.Stepway.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class AccountRecoveryService {
    private final UserRepository users;
    private final AccountTokenRepository tokens;
    private final AccountEmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String frontendUrl;

    public AccountRecoveryService(UserRepository users,
                                  AccountTokenRepository tokens,
                                  AccountEmailService emailService,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${app.frontend-url}") String frontendUrl) {
        this.users = users;
        this.tokens = tokens;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.frontendUrl = trimTrailingSlash(frontendUrl);
    }

    @Transactional
    public void sendVerification(User user) {
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }
        String token = createToken(user, AccountToken.Purpose.EMAIL_VERIFICATION, 24, ChronoUnit.HOURS);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(),
                frontendUrl + "/verify-email.html?token=" + token);
    }

    @Transactional
    public void requestVerification(String email) {
        User user = findUser(email);
        if (user != null) {
            sendVerification(user);
        }
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        AccountToken token = requireActive(rawToken, AccountToken.Purpose.EMAIL_VERIFICATION);
        User user = token.getUser();
        user.setEmailVerified(Boolean.TRUE);
        token.setUsedAt(Instant.now());
        users.save(user);
        tokens.save(token);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = findUser(email);
        if (user == null) {
            return;
        }
        String token = createToken(user, AccountToken.Purpose.PASSWORD_RESET, 1, ChronoUnit.HOURS);
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(),
                frontendUrl + "/reset-password.html?token=" + token);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        AccountToken token = requireActive(rawToken, AccountToken.Purpose.PASSWORD_RESET);
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        token.setUsedAt(Instant.now());
        users.save(user);
        tokens.save(token);
    }

    private String createToken(User user, AccountToken.Purpose purpose, long amount, ChronoUnit unit) {
        String rawToken = randomToken();
        AccountToken token = new AccountToken();
        token.setUser(user);
        token.setPurpose(purpose);
        token.setTokenHash(hash(rawToken));
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(amount, unit));
        tokens.save(token);
        return rawToken;
    }

    private AccountToken requireActive(String rawToken, AccountToken.Purpose purpose) {
        if (!StringUtils.hasText(rawToken)) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid or expired token");
        }
        AccountToken token = tokens.findByTokenHashAndPurpose(hash(rawToken), purpose)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Invalid or expired token"));
        if (!token.isActive(Instant.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid or expired token");
        }
        return token;
    }

    private User findUser(String email) {
        return StringUtils.hasText(email) ? users.findByEmail(email.trim().toLowerCase()) : null;
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder value = new StringBuilder();
            for (byte b : hashed) {
                value.append(String.format("%02x", b));
            }
            return value.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
