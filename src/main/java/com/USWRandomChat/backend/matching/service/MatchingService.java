package com.USWRandomChat.backend.matching.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private static final long MAX_MATCHING_TIME = 120000; //2분을 밀리초로 표현한 상수
    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final JwtProvider jwtProvider;
    private final String randomQueue = "MatchQueue";
    private final RedisTemplate<String, String> matchRedisTemplate;
    private ZSetOperations<String, String> matchQueue;

    @PostConstruct
    public void init() {
        matchQueue = matchRedisTemplate.opsForZSet();
    }

    //매칭 큐 참가
    public void addToMatchingQueue(String accessToken) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출
        String account = jwtProvider.getAccount(accessToken);

        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
        matchQueue.add(randomQueue, member.getAccount(), System.currentTimeMillis());
    }

    //매칭 큐 취소
    public void removeCancelParticipants(String accessToken) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출
        String account = jwtProvider.getAccount(accessToken);

        matchQueue.remove(randomQueue, account);
        log.info("매칭 취소 회원: {} 그리고 큐에서 지웠습니다.", account);
    }
    
    //매칭 알고리즘
    public String performMatching() {
        if (matchQueue.size(randomQueue) < 2) {
            log.info("매칭할 회원 수가 부족합니다.");
            return null;
        }

        String participant1 = matchQueue.range(randomQueue, 0, 0).iterator().next();
        String participant2 = matchQueue.range(randomQueue, 1, 1).iterator().next();

        String chatRoomId = UUID.randomUUID().toString();

        updateMemberRoomId(participant1, chatRoomId);
        updateMemberRoomId(participant2, chatRoomId);

        matchQueue.remove(randomQueue, participant1, participant2);
        log.info("매칭된 회원들을 {} 방에 매칭하였습니다.", chatRoomId);

        return chatRoomId; //매칭된 채팅방 ID 반환
    }
    
    //시간 초과한 회원의 매칭 취소
    public void removeExpiredParticipants() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredParticipants = matchQueue.rangeByScore(randomQueue, 0, currentTime - MAX_MATCHING_TIME);
        expiredParticipants.forEach(expiredParticipant -> matchQueue.remove(randomQueue, expiredParticipant));
        log.info("매칭 시간이 2분 초과하여 매칭 취소된 회원: {}", expiredParticipants);
    }

    //각 회원의 roomId를 업데이트하는 메서드
    private void updateMemberRoomId(String account, String roomId) {
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        Profile profile = profileRepository.findByMember(member)
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

        profile.setRoomId(roomId);
        profileRepository.save(profile);
        log.info("{}의 roomId를 {}로 업데이트하였습니다.", account, roomId);
    }
}
