package com.peoples.api.repository;

import com.peoples.api.domain.EmailAuthToken;
import com.peoples.api.domain.UserStudyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailAuthTokenRepository extends JpaRepository<EmailAuthToken, String> {
    Optional<EmailAuthToken> findByIdAndExpirationDateAfterAndExpired(String emailAuthTokenId, LocalDateTime now, boolean expired);
}
