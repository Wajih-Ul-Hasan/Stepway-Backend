package com.example.Stepway.Repository;

import com.example.Stepway.Domain.AccountToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {
    Optional<AccountToken> findByTokenHashAndPurpose(String tokenHash, AccountToken.Purpose purpose);
}
