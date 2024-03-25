package com.USWRandomChat.backend.email.repository;

import com.USWRandomChat.backend.email.domain.EmailToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface EmailTokenRepository extends JpaRepository<EmailToken, String>{

    Optional<EmailToken> findByUuidAndExpirationDateAfterAndExpired(String uuid, LocalDateTime now, boolean expired);
    Optional<EmailToken> findByUuid(String uuid);
    List<EmailToken> findByExpirationDateBeforeAndExpiredIsFalse(LocalDateTime expirationTime);
    void deleteByUuid(String uuid);
}