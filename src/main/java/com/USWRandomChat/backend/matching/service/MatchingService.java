package com.USWRandomChat.backend.matching.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
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

    private static final long MAX_MATCHING_TIME = 120000; // 2분을 밀리초로 표현한 상수
    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final String randomQueue = "MatchQueue";
    private final RedisTemplate<String, String> matchRedisTemplate;
    private ZSetOperations<String, String> matchQueue;

    @PostConstruct
    public void init() {
        matchQueue = matchRedisTemplate.opsForZSet();
    }

    public void addToMatchingQueue(String account) {
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
        matchQueue.add(randomQueue, member.getAccount(), System.currentTimeMillis());
    }

    public void removeCancelParticipants(String account) {
        matchQueue.remove(randomQueue, account);
        log.info("매칭 취소 회원: {} 그리고 큐에서 지웠습니다.", account);
    }

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

        return chatRoomId; // 매칭된 채팅방 ID 반환
    }

    public void removeExpiredParticipants() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredParticipants = matchQueue.rangeByScore(randomQueue, 0, currentTime - MAX_MATCHING_TIME);
        expiredParticipants.forEach(expiredParticipant -> matchQueue.remove(randomQueue, expiredParticipant));
        log.info("매칭 시간이 2분 초과하여 매칭 취소된 회원: {}", expiredParticipants);
    }

    // 각 회원의 roomId를 업데이트하는 메서드
    private void updateMemberRoomId(String account, String roomId) {
        // account를 사용하여 Member 객체를 조회
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
        matchQueue.add(randomQueue, member.getAccount(), System.currentTimeMillis());

        // 해당 Member와 연관된 Profile 조회
        Profile profile = profileRepository.findByMember(member);
        if (profile == null) {
            // Profile이 존재하지 않는 경우 로그 출력 (또는 새로운 Profile을 생성하고 저장할 수도 있습니다)
            log.info("{}에 해당하는 프로필을 찾을 수 없습니다.", account);
            return;
        }

        // Profile이 존재하면 roomId 업데이트 후 저장
        profile.setRoomId(roomId);
        profileRepository.save(profile);
    }
}