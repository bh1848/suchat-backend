package com.USWRandomChat.backend.emailAuth.service;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import com.USWRandomChat.backend.member.service.MemberService;
import com.USWRandomChat.backend.emailAuth.repository.EmailTokenRepository;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAuthSchedulerService {

    private final MemberService memberService;
    private final EmailTokenRepository emailTokenRepository;

    @Transactional(readOnly = true)
    public List<EmailToken> findExpiredFalse(LocalDateTime localDateTime) {
        return emailTokenRepository.findByExpirationDateBeforeAndExpiredIsFalse(localDateTime.minusMinutes(30));
    }

    //유령회원 지우기
    @Transactional
    @Scheduled(cron = "0 * * * * * ")
    public void removeMember() {
        log.info("{} - 이메일 인증을 수행하지 않은 유저 검증 시작", LocalDateTime.now());
        List<EmailToken> emailTokens = findExpiredFalse(LocalDateTime.now());

        for (EmailToken emailToken : emailTokens) {
            Long account = emailToken.getMember().getId();
            memberService.deleteFromId(account);

        }
        log.info("{} - 이메일 인증을 수행하지 않은 유저 검증 종료", LocalDateTime.now());
    }
}
