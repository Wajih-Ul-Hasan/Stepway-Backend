package com.example.Stepway.Service.Impl;

import com.example.Stepway.Domain.AccountToken;
import com.example.Stepway.Domain.User;
import com.example.Stepway.Repository.AccountTokenRepository;
import com.example.Stepway.Repository.UserRepository;
import com.example.Stepway.Service.impl.AccountEmailService;
import com.example.Stepway.Service.impl.AccountRecoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AccountRecoveryServiceTest {
    @Test
    void verificationMarksUserVerifiedAndConsumesToken() {
        User user = new User();
        user.setEmail("student@example.com");
        user.setFirstName("Student");
        user.setEmailVerified(false);
        UserRepository users = mock(UserRepository.class);
        AccountTokenRepository tokens = tokenRepository();
        AccountEmailService email = mock(AccountEmailService.class);
        AccountRecoveryService service = new AccountRecoveryService(users, tokens, email,
                new BCryptPasswordEncoder(), "https://stepway.example");

        service.sendVerification(user);
        String link = captureVerificationLink(email);
        service.verifyEmail(link.substring(link.indexOf("token=") + 6));

        assertTrue(user.getEmailVerified());
        verify(users).save(user);
    }

    @Test
    void passwordResetUpdatesPasswordAndUsesGenericUnknownEmailResponse() {
        User user = new User();
        user.setEmail("student@example.com");
        user.setFirstName("Student");
        user.setPassword(new BCryptPasswordEncoder().encode("old-password"));
        UserRepository users = mock(UserRepository.class);
        when(users.findByEmail("student@example.com")).thenReturn(user);
        AccountTokenRepository tokens = tokenRepository();
        AccountEmailService email = mock(AccountEmailService.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        AccountRecoveryService service = new AccountRecoveryService(users, tokens, email,
                encoder, "https://stepway.example");

        service.requestPasswordReset("student@example.com");
        String link = captureResetLink(email);
        service.resetPassword(link.substring(link.indexOf("token=") + 6), "new-password-123");
        service.requestPasswordReset("missing@example.com");

        assertTrue(encoder.matches("new-password-123", user.getPassword()));
        verify(users).save(user);
        verify(email, times(1)).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    private AccountTokenRepository tokenRepository() {
        AccountTokenRepository tokens = mock(AccountTokenRepository.class);
        AtomicReference<AccountToken> stored = new AtomicReference<>();
        when(tokens.save(any(AccountToken.class))).thenAnswer(invocation -> {
            AccountToken token = invocation.getArgument(0);
            stored.set(token);
            return token;
        });
        when(tokens.findByTokenHashAndPurpose(anyString(), any(AccountToken.Purpose.class))).thenAnswer(invocation -> {
            AccountToken token = stored.get();
            return token != null && token.getTokenHash().equals(invocation.getArgument(0))
                    && token.getPurpose() == invocation.getArgument(1) ? Optional.of(token) : Optional.empty();
        });
        return tokens;
    }

    private String captureVerificationLink(AccountEmailService email) {
        org.mockito.ArgumentCaptor<String> link = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(email).sendVerificationEmail(eq("student@example.com"), eq("Student"), link.capture());
        return link.getValue();
    }

    private String captureResetLink(AccountEmailService email) {
        org.mockito.ArgumentCaptor<String> link = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(email).sendPasswordResetEmail(eq("student@example.com"), eq("Student"), link.capture());
        return link.getValue();
    }
}
