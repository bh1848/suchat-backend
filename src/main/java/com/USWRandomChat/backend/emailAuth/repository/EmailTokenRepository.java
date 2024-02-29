package com.USWRandomChat.backend.emailAuth.repository;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface EmailTokenRepository extends JpaRepository<EmailToken, String>{

    Optional<EmailToken> findByUuidAndExpirationDateAfterAndExpired(String uuid, LocalDateTime now, boolean expired);
    Optional<EmailToken> findByUuid(String uuid);
    List<EmailToken> findByExpirationDateBeforeAndExpiredIsFalse(LocalDateTime expirationTime);
    void deleteByUuid(String uuid);
    
    //member_id로 이메일 토큰 삭제하기 위해 필요
    EmailToken findByMember(MemberTemp memberTemp);

}