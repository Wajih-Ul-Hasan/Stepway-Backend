package com.example.Stepway.Service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AccountEmailService {
    private static final Logger log = LoggerFactory.getLogger(AccountEmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String smtpHost;
    private final String smtpUsername;
    private final String smtpPassword;

    public AccountEmailService(JavaMailSender mailSender,
                               @Value("${app.email.from:}") String fromAddress,
                               @Value("${spring.mail.host:}") String smtpHost,
                               @Value("${spring.mail.username:}") String smtpUsername,
                               @Value("${spring.mail.password:}") String smtpPassword) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.smtpHost = smtpHost;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    public void sendVerificationEmail(String email, String firstName, String verifyUrl) {
        send(email, "Verify your Stepway email",
                "Hi " + displayName(firstName) + ",\n\n"
                        + "Please verify your Stepway account by opening this link:\n"
                        + verifyUrl + "\n\n"
                        + "This link expires in 24 hours.");
    }

    public void sendPasswordResetEmail(String email, String firstName, String resetUrl) {
        send(email, "Reset your Stepway password",
                "Hi " + displayName(firstName) + ",\n\n"
                        + "Open this link to reset your Stepway password:\n"
                        + resetUrl + "\n\n"
                        + "This link expires in 1 hour. If you did not request it, ignore this email.");
    }

    private void send(String to, String subject, String body) {
        if (!mailConfigured()) {
            log.warn("Email not sent because SMTP is not configured. Subject={}, To={}", subject, to);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        if (StringUtils.hasText(fromAddress)) {
            message.setFrom(fromAddress);
        }
        mailSender.send(message);
    }

    private boolean mailConfigured() {
        return StringUtils.hasText(smtpHost)
                && StringUtils.hasText(smtpUsername)
                && StringUtils.hasText(smtpPassword);
    }

    private String displayName(String firstName) {
        return StringUtils.hasText(firstName) ? firstName.trim() : "there";
    }
}
