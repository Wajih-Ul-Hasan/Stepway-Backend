package com.example.Stepway.Domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "account_token", indexes = {
        @Index(name = "idx_account_token_hash", columnList = "tokenHash", unique = true),
        @Index(name = "idx_account_token_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class AccountToken {
    public enum Purpose {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Purpose purpose;

    @Column(nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant usedAt;

    public boolean isActive(Instant now) {
        return usedAt == null && expiresAt.isAfter(now);
    }
}
