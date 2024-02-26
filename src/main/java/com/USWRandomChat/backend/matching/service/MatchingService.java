package com.USWRandomChat.backend.matching.service;

import com.USWRandomChat.backend.exception.ExceptionType;
import com.USWRandomChat.backend.exception.errortype.AccountException;
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
import java.util.Optional;
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

    // 매칭 큐에 회원 추가
    public void addToMatchingQueue(String account) {
        Member member = memberRepository.findByAccount(account);
        if (member == null) {
            throw new AccountException(ExceptionType.USER_NOT_EXISTS);
        }
        matchQueue.add(randomQueue, member.getAccount(), System.currentTimeMillis());
    }

    // 매칭 취소 시 randomQueue에서 회원 삭제
    public void removeCancelParticipants(String account) {
        matchQueue.remove(randomQueue, account);
        log.info("매칭 취소 회원: {} 그리고 큐에서 지웠습니다.", account);
    }

    // MatchingService 내 performMatching 메서드 수정
    public String[] performMatching() {
        long size = matchQueue.size(randomQueue);
        if (size < 2) {
            log.info("큐에 있는 회원: 매칭할 회원 수가 부족합니다.");
            return null; // 매칭할 회원 수가 부족할 때 null 반환
        }

        String participant1 = matchQueue.range(randomQueue, 0, 0).iterator().next(); // 첫 번째 사용자
        String participant2 = matchQueue.range(randomQueue, 1, 1).iterator().next(); // 두 번째 사용자

        String chatRoomId = UUID.randomUUID().toString(); // 매칭된 회원 2명의 채팅방 uuid 생성

        updateMemberRoomId(participant1, chatRoomId);
        updateMemberRoomId(participant2, chatRoomId);

        matchQueue.remove(randomQueue, participant1, participant2); // Queue에서 매칭된 사용자 제거
        log.info("{} 및 {} 회원을 {} 방에 매칭", participant1, participant2, chatRoomId);

        return new String[]{participant1, participant2, chatRoomId}; // 매칭된 회원과 채팅방 ID 반환
    }

    // randomQueue에서 2분이 지난 회원 삭제
    public void removeExpiredParticipants() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredParticipants = matchQueue.rangeByScore(randomQueue, 0, currentTime - MAX_MATCHING_TIME);
        expiredParticipants.forEach(expiredParticipant -> matchQueue.remove(randomQueue, expiredParticipant));
        log.info("매칭 시간이 2분 초과하여 매칭 취소된 회원: {}", expiredParticipants);
    }

    // 각 회원의 roomId를 업데이트하는 메서드
    private void updateMemberRoomId(String account, String roomId) {
        // account를 사용하여 Member 객체를 조회
        Member member = memberRepository.findByAccount(account);
        if (member == null) {
            // Member가 존재하지 않는 경우 예외 발생
            throw new AccountException(ExceptionType.USER_NOT_EXISTS);
        }

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
