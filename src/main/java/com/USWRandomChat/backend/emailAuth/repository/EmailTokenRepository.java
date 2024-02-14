package com.USWRandomChat.backend.emailAuth.repository;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface EmailTokenRepository extends JpaRepository<EmailToken, String>{

    Optional<EmailToken> findByUuidAndExpirationDateAfterAndExpired(String uuid, LocalDateTime now, boolean expired);
    Optional<EmailToken> findByUuid(String uuid);
    List<EmailToken> findByExpirationDateBeforeAndExpiredIsFalse(LocalDateTime expirationTime);
    void deleteByUuid(String uuid);

    // Member의 id와 연관된 EmailToken 삭제
    void deleteByMemberId(Long memberId);
}