package com.USWRandomChat.backend.room.service;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.exception.MemberNotFoundException;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoomService {
    private static final long MAX_MATCHING_TIME = 120000; // 2분을 밀리초로 표현한 상수
    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final String randomQueue = "MatchQueue";
    private ZSetOperations<String, String> matchQueue;

    // randomQueue에 회원 삽입
    public void addToMatchingQueue(String memberId) {
        Optional<Member> optionalMember = Optional.ofNullable(memberRepository.findByAccount(memberId));

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            matchQueue.add(randomQueue, memberId, System.currentTimeMillis());
        } else {
            log.info("사용자를 찾을 수 없습니다. memberId: " + memberId);
        }
    }

    // 매칭 취소 시 randomQueue에서 회원 삭제
    public void removeCancelParticipants(String memberId) {
        // 해당 memberId를 matchQueue에서 제거
        matchQueue.remove(randomQueue, memberId);

        log.info("매칭 취소 회원: " + memberId + "그리고 큐에서 지웠습니다.");
    }

    public String[] performMatching() {
        int queueSize = Math.toIntExact(matchQueue.size(randomQueue));

        if (queueSize < 2) {
            log.info("매칭할 회원 수가 부족합니다. 현재 큐에 있는 회원 수: " + queueSize);
            return null;
        }

        String participant1 = Objects.requireNonNull(matchQueue.range(randomQueue, 0, 0)).iterator().next();
        String participant2 = Objects.requireNonNull(matchQueue.range(randomQueue, 1, 1)).iterator().next();

        // 매칭된 회원 2명의 채팅방 uuid 생성
        String chatRoomId = UUID.randomUUID().toString();

        // 각 회원의 roomId 업데이트
        updateMemberRoomId(participant1, chatRoomId);
        updateMemberRoomId(participant2, chatRoomId);

        // Queue에서 매칭된 사용자 제거
        matchQueue.remove(randomQueue, participant1, participant2);

        // participant1, participant2, chatRoomId를 배열에 담아 반환
        return new String[]{participant1, participant2, chatRoomId};
    }

    // randomQueue 에서 2분이 지난 회원 삭제
    public void removeExpiredParticipants() {
        // 현재 시간
        long currentTime = System.currentTimeMillis();

        // 매칭 큐에서 2분 이상 대기한 사용자를 찾아서 삭제
        Set<String> expiredParticipants = matchQueue.rangeByScore(randomQueue, 0, currentTime - MAX_MATCHING_TIME);

        for (String expiredParticipant : expiredParticipants) {
            // 2분 이상 대기한 사용자를 매칭 큐에서 제거
            matchQueue.remove(randomQueue, expiredParticipant);
            log.info(expiredParticipant + " 사용자의 매칭 시간이 2분 초과하여 매칭 취소되었습니다.");
        }
    }

    // 각 회원의 roomId를 업데이트하는 메서드
    private void updateMemberRoomId(String memberId, String roomId) {
        Optional<Profile> profileOptional = profileRepository.findByMemberAccount(memberId);
        if (profileOptional.isPresent()) {
            Profile profile = profileOptional.get();
            profile.setRoomId(roomId);
        } else {
            // 해당 계정에 대한 profile이 존재하지 않는 경우
            log.info(memberId + " 사용자에 대한 profile이 존재하지 않습니다.");
        }
    }
}
