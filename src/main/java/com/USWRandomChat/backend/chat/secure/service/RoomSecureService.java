package com.USWRandomChat.backend.chat.secure.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoomSecureService {
    private static final String MATCH_QUEUE = "MatchQueue"; // 매칭 큐의 Redis key

    private final ProfileRepository profileRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> matchRedisTemplate;
    private ZSetOperations<String, String> matchQueue;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostConstruct
    public void init() {
        matchQueue = matchRedisTemplate.opsForZSet();
    }

    //매칭 참가
    public void addToMatchingQueue(String account) {
        matchQueue.add(MATCH_QUEUE, account, System.currentTimeMillis());
        messagingTemplate.convertAndSend("/queue/match/in/" + account, "매칭 요청이 접수되었습니다.");
        performMatchingAsync(account);
    }

    //매칭 취소
    public void removeCancelParticipants(String account) {
        matchQueue.remove(MATCH_QUEUE, account);
        messagingTemplate.convertAndSend("/queue/match/cancel/" + account, "매칭이 취소되었습니다.");
        log.info("매칭 취소 회원: {} 큐에서 제거", account);
    }

    //매칭 알고리즘
    @Async
    public CompletableFuture<String> performMatchingAsync(String account) {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (matchQueue.size(MATCH_QUEUE) > 1) {
            String participant1 = Objects.requireNonNull(matchQueue.range(MATCH_QUEUE, 0, 0)).iterator().next();
            String participant2 = Objects.requireNonNull(matchQueue.range(MATCH_QUEUE, 1, 1)).iterator().next();
            String chatRoomId = UUID.randomUUID().toString();

            updateMemberRoomId(participant1, chatRoomId);
            updateMemberRoomId(participant2, chatRoomId);

            sendMatchingNotification(participant1, chatRoomId, participant2);
            sendMatchingNotification(participant2, chatRoomId, participant1);

            log.info("[매칭 결과] 참가자1: {}\t참가자2: {}", participant1, participant2);

            matchQueue.remove(MATCH_QUEUE, participant1);
            matchQueue.remove(MATCH_QUEUE, participant2);
            log.info("매칭된 회원들을 {} 방에 매칭하였습니다.", chatRoomId);

            future.complete(chatRoomId);
        } else {
            future.completeExceptionally(new IllegalStateException("매칭 큐에 충분한 참가자가 없습니다."));
        }

        return future;
    }

    //매칭 완료
    private void sendMatchingNotification(String account, String chatRoomId, String targetAccount) {
        messagingTemplate.convertAndSend("/queue/match/in/" + account, "매칭완료 " + chatRoomId + " " + targetAccount);
        log.info("{} 사용자에게 매칭 메시지 전달 성공", account);
    }

    public void updateMemberRoomId(String account, String roomId) {
        Member member = findMemberByAccount(account);
        Profile profile = findProfileByMember(member);

        profile.setRoomId(roomId);
        profileRepository.save(profile);
        log.info("{}의 roomId를 {}로 업데이트", account, roomId);
    }

    private Member findMemberByAccount(String account) {
        return memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
    }

    private Profile findProfileByMember(Member member) {
        return profileRepository.findByMember(member)
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
    }

    //테스트 용도
    public String checkMemberRoomId(String account) {
        Member member = findMemberByAccount(account);
        Profile profile = findProfileByMember(member);
        return profile.getRoomId();
    }
}