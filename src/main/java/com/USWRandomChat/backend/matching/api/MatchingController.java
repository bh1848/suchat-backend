package com.USWRandomChat.backend.matching.api;

import com.USWRandomChat.backend.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/match")
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/in")
    public ResponseEntity<?> randomMatch(@RequestParam String account) {
        try {
            matchingService.addToMatchingQueue(account); // 매칭 전에 큐에 회원 추가
            matchingService.removeExpiredParticipants(); // 만료된 참가자 제거
            String[] matchedMembers = matchingService.performMatching(); // 매칭 시도

            if (matchedMembers == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("매칭할 회원 수가 부족합니다.");
            } else {
                return ResponseEntity.ok(matchedMembers); // 매칭된 회원 정보와 채팅방 ID 반환
            }
        } catch (Exception e) {
            log.error("매칭 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("매칭 중 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelMatch(@RequestParam String account) {
        matchingService.removeCancelParticipants(account);
        return ResponseEntity.status(HttpStatus.OK).body("매칭 취소 성공");
    }
}
