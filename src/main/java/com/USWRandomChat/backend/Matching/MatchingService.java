package com.USWRandomChat.backend.Matching;

import java.util.*;

import static com.sun.activation.registries.LogSupport.log;

public class MatchingService {
    private List<Participant> participantList;

    public MatchingService() {
        this.participantList = new ArrayList<>();
        // 매칭 주기를 5초로 설정 (원하는 주기로 조절)
        int matchingInterval = 5000; // 5 seconds
        // 주기적으로 매칭을 수행하는 TimerTask 설정
        Timer matchingTimer = new Timer(true);
        matchingTimer.scheduleAtFixedRate(new MatchingTask(), 0, matchingInterval);
    }

    // 매칭 시작 시 사용자를 매칭 리스트에 추가
    public void addToMatchingMemberList(Participant participant) {
        participantList.add(participant);
    }

    // 매칭 알고리즘 구현
    // 리스트에 있는 사용자를 셔플한 뒤 2명끼리 매칭
    // 주기적으로 매칭 리스트를 리프레시
    // 매칭 알고리즘 수행
    private List<MatchingParticipants> performMatching() {
        // 매칭된 사용자들을 저장할 리스트
        List<MatchingParticipants> matchedTeams = new ArrayList<>();

        // 매칭 리스트를 섞음
        Collections.shuffle(participantList);

        // 매칭된 사용자를 추출하여 채팅방 생성
        for (int i = 0; i < participantList.size(); i += 2) {
            if (i + 1 < participantList.size()) {
                Participant participant1 = participantList.get(i);
                Participant participant2 = participantList.get(i + 1);

                // createPair 메서드를 사용하여 매칭된 팀 리스트 생성
                MatchingParticipants matchedTeam = MatchingParticipants.createPair(participant1.getParticipantId(), participant2.getParticipantId());

                // 매칭된 팀을 리스트에 추가
                matchedTeams.add(matchedTeam);
            }
        }
        // 매칭된 사용자들을 담은 리스트를 반환
        return matchedTeams;
    }

    // TimerTask를 이용하여 주기적으로 매칭 수행
    private class MatchingTask extends TimerTask {
        @Override
        public void run() {
            performMatching();
        }
    }
}
