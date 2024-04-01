package com.USWRandomChat.backend.emailAuth.repository;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface EmailTokenRepository extends JpaRepository<EmailToken, String>{

    Optional<EmailToken> findByUuidAndExpirationDateAfterAndExpired(String uuid, LocalDateTime now, boolean expired);
    @EntityGraph(attributePaths = "memberTemp")
    Optional<EmailToken> findByUuid(String uuid);
    List<EmailToken> findByExpirationDateBeforeAndExpiredIsFalse(LocalDateTime expirationTime);
}