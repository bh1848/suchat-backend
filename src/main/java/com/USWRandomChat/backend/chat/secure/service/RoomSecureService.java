package com.USWRandomChat.backend.chat.secure.service;

import com.USWRandomChat.backend.chat.repository.MessageRepository;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ChatException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.security.jwt.service.AuthenticationService;
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
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoomSecureService {
    private static final long MAX_MATCHING_TIME = 120000; //2분을 밀리초로 표현한 상수
    private static final String MATCH_QUEUE = "MatchQueue"; //매칭 큐의 Redis key

    private final MessageRepository messageRepository;
    private final ProfileRepository profileRepository;
    private final MemberRepository memberRepository;
    private final AuthenticationService authenticationService;
    private final RedisTemplate<String, String> matchRedisTemplate;
    private ZSetOperations<String, String> matchQueue;

    @PostConstruct
    public void init() {
        matchQueue = matchRedisTemplate.opsForZSet();
    }
    
    //메세지 삭제
    public void deleteRoomIdMessage(String roomId) {
        executeWithChatExceptionHandling(() -> messageRepository.deleteByRoomId(roomId),
                String.format("Room ID %s에 대한 메시지가 성공적으로 삭제되었습니다.", roomId),
                String.format("Room ID %s 메시지 삭제 중 오류 발생", roomId));
    }
    
    //매칭 큐 참가
    public void addToMatchingQueue(HttpServletRequest request) {
        Member member = authenticationService.getAuthenticatedMember(request);
        matchQueue.add(MATCH_QUEUE, member.getAccount(), System.currentTimeMillis());
    }

    //매칭 취소
    public void removeCancelParticipants(HttpServletRequest request) {
        Member member = authenticationService.getAuthenticatedMember(request);
        matchQueue.remove(MATCH_QUEUE, member.getAccount());
        log.info("매칭 취소 회원: {} 그리고 큐에서 지웠습니다.", member.getAccount());
    }
    
    //매칭 알고리즘
    public String performMatching() {
        if (matchQueue.size(MATCH_QUEUE) < 2) {
            log.info("매칭할 회원 수가 부족합니다.");
            return null;
        }

        String participant1 = Objects.requireNonNull(matchQueue.range(MATCH_QUEUE, 0, 0)).iterator().next();
        String participant2 = Objects.requireNonNull(matchQueue.range(MATCH_QUEUE, 1, 1)).iterator().next();
        String chatRoomId = UUID.randomUUID().toString();

        updateMemberRoomId(participant1, chatRoomId);
        updateMemberRoomId(participant2, chatRoomId);

        matchQueue.remove(MATCH_QUEUE, participant1, participant2);
        log.info("매칭된 회원들을 {} 방에 매칭하였습니다.", chatRoomId);

        return chatRoomId;
    }
    
    //매칭 시간 초과된 회원 취소
    public void removeExpiredParticipants() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredParticipants = matchQueue.rangeByScore(MATCH_QUEUE, 0, currentTime - MAX_MATCHING_TIME);
        assert expiredParticipants != null;
        expiredParticipants.forEach(expiredParticipant -> matchQueue.remove(MATCH_QUEUE, expiredParticipant));
        log.info("매칭 시간이 2분 초과하여 매칭 취소된 회원: {}", expiredParticipants);
    }
    
    //룸 Id 업데이트
    private void updateMemberRoomId(String account, String roomId) {
        Member member = findMemberByAccount(account);
        Profile profile = findProfileByMember(member);

        profile.setRoomId(roomId);
        profileRepository.save(profile);
        log.info("{}의 roomId를 {}로 업데이트하였습니다.", account, roomId);
    }

    private Member findMemberByAccount(String account) {
        return memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
    }

    private Profile findProfileByMember(Member member) {
        return profileRepository.findByMember(member)
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
    }

    //방에 남아 있는 인원 수를 확인
    public int countRemainingMembers(String roomId) {
        try {
            List<Profile> remainingMembers = profileRepository.findAllProfilesByRoomId(roomId);
            return remainingMembers.size();
        } catch (Exception e) {
            log.error("Room ID {}에 대한 남은 인원 수 확인 중 오류 발생: {}", roomId, e.getMessage());
            throw new ChatException(ExceptionType.MEMBER_COUNT_ERROR);
        }
    }

    //특정 사용자의 roomId를 'none'으로 초기화
    public void exitRoomId(HttpServletRequest request, String exitRoomId) {
        try {
            Member member = authenticationService.getAuthenticatedMember(request);
            updateProfileRoomIdToNone(member, exitRoomId);
        } catch (AccountException | ChatException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Room ID {} 업데이트 중 예외 발생: {}", exitRoomId, e.getMessage());
            throw new ChatException(ExceptionType.ROOM_ID_UPDATE_ERROR);
        }
    }

    //특정 Member의 roomId를 'none'으로 업데이트
    private void updateProfileRoomIdToNone(Member member, String exitRoomId) {
        Optional<Profile> profileOpt = profileRepository.findByMemberAndRoomId(member, exitRoomId);

        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            profile.setRoomId("none");
            profileRepository.save(profile);
            log.info("계정 {}의 Room ID가 'none'으로 성공적으로 설정되었습니다.", member.getAccount());
        } else {
            throw new ProfileException(ExceptionType.PROFILE_NOT_EXISTS);
        }
    }

    //예외 처리
    private void executeWithChatExceptionHandling(Runnable task, String successLogMessage, String errorLogMessage) {
        try {
            task.run();
            log.info(successLogMessage);
        } catch (Exception e) {
            log.error("{}: {}", errorLogMessage, e.getMessage());
            throw new ChatException(ExceptionType.MESSAGE_DELETE_ERROR);
        }
    }
}