package com.USWRandomChat.backend.chat.service;

import com.USWRandomChat.backend.chat.domain.MatchParticipants;
import com.USWRandomChat.backend.chat.repository.MatchParticipantsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import java.util.List;
import java.util.UUID;
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final RedisTemplate<String, String> matchredisTemplate;
    private final MatchParticipantsRepository matchParticipantsRepository;
    public ZSetOperations<String, String> addToMatchingQueue(String memberId, String key) {

        //Sorted Set 연산자 구현체인 matchQueue 생성, 멤버를 추가, 현재 시간을 score로 잡음, key=MatchQueue
        ZSetOperations<String, String> matchQueue = matchredisTemplate.opsForZSet();
        matchQueue.add(key, memberId, System.currentTimeMillis());

        return matchQueue;
    }

    //매칭 알고리즘 구현
    public List<MatchParticipants> perFormMatching(ZSetOperations<String, String> matchQueue, String key){
        //매칭된 사용자들을 저장할 리스트
        List<MatchParticipants> matchedTeams = new ArrayList<>();

        for (; matchQueue.size(key) >= 2; ) {

            // 2명 이상이면 매칭 수행
            String participant1 = matchQueue.range(key, 0, 0).iterator().next(); // 첫 번째 사용자
            String participant2 = matchQueue.range(key, 1, 1).iterator().next(); // 두 번째 사용자

            // MatchingParticipants 생성 및 매칭된 사용자들 리스트에 추가
            MatchParticipants matchedTeam = MatchParticipants.createPair(participant1, participant2);
            matchedTeams.add(matchedTeam);
            // Queue에서 매칭된 사용자 제거
            //matchQueue.remove(key, participant1, participant2);
        }
        // 매칭된 팀을 저장
        saveMatchedTeams(matchedTeams);

        // 2명 미만일 때 매칭 실패
        if (matchQueue.size(key) < 2 && matchQueue.size(key) > 0) {
            System.out.println("매칭 실패: 매칭 대기 중인 회원 수 부족");
        }

        // 매칭된 사용자들을 담은 리스트를 반환
        return matchedTeams;

    }

    //매칭 성공 시 채팅방 생성
    public List<MatchParticipants> matchingSuccess(List<MatchParticipants> matchedTeams) {

        //채팅방 아이디 담긴 chatRoomTeams 담는 리스트
        List<MatchParticipants> chatRoomTeams = new ArrayList<>();

        for (int i = 0; i < matchedTeams.size(); i++) {
            //채팅방 uuid 생성
            String chatRoomId = UUID.randomUUID().toString();

            //매칭된 회원 테이블에 채팅방(uuid) 추가
            MatchParticipants chatRoomTeam = matchedTeams.get(i);
            chatRoomTeam.setChatRoomId(chatRoomId);

            chatRoomTeams.add(chatRoomTeam);
        }
        return chatRoomTeams;
    }

    //매칭된 팀을 저장
    private void saveMatchedTeams(List<MatchParticipants> matchedTeams) {
        for (MatchParticipants matchedTeam : matchedTeams) {
            matchParticipantsRepository.save(matchedTeam);
        }
    }

}

