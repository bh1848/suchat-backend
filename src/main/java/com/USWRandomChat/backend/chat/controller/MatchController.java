package com.USWRandomChat.backend.chat.controller;


import com.USWRandomChat.backend.chat.domain.MatchParticipants;
import com.USWRandomChat.backend.chat.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchingService matchingService;

    @PostMapping("/live")
    @Transactional
    public List<MatchParticipants> randomMatch (@RequestParam String memberId, @RequestParam String key) {
        ZSetOperations<String, String> MatchQueue = matchingService.addToMatchingQueue(memberId, key);
        List<MatchParticipants> matchedTeams = matchingService.perFormMatching(MatchQueue, key);
        List<MatchParticipants> chatRoomTeams = matchingService.matchingSuccess(matchedTeams);
        return chatRoomTeams;
    }

    @PostMapping("/addToMatchingQueue")
    public void addToMatchingQueue(@RequestParam String memberId, @RequestParam String key) {
        matchingService.addToMatchingQueue(memberId, key);
    }

    @PostMapping("/performMatching")
    public List<MatchParticipants> performMatching(@RequestParam ZSetOperations<String, String> matchQueue, @RequestParam String key) {
        return matchingService.perFormMatching(matchQueue, key);
    }

    @PostMapping("/matchingSuccess")
    public List<MatchParticipants> matchingSuccess(@RequestParam List<MatchParticipants> matchedTeams) {
        return matchingService.matchingSuccess(matchedTeams);
    }


}
