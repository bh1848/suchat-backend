package com.USWRandomChat.backend.repository;

import com.USWRandomChat.backend.domain.EmailToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface EmailTokenRepository extends JpaRepository<EmailToken, String>{

    Optional<EmailToken> findByUuidAndExpirationDateAfterAndExpired(String uuid, LocalDateTime now, boolean expired);
    Optional<EmailToken> findByUuid(String uuid);
    Optional<EmailToken> findById(String uuid);
    List<EmailToken> findByExpirationDateBeforeAndExpiredIsFalse(LocalDateTime expirationTime);
    void deleteByUuid(String uuid);
}