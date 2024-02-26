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
            // 매칭 전에 큐에 회원 추가
            matchingService.addToMatchingQueue(account);
            // 만료된 참가자 제거
            matchingService.removeExpiredParticipants();
            // 매칭 시도하고 채팅방 ID 반환
            String chatRoomId = matchingService.performMatching();

            if (chatRoomId == null) {
                // 매칭할 회원 수가 부족할 경우
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("매칭할 회원 수가 부족합니다.");
            } else {
                // 성공적으로 매칭된 경우, 채팅방 ID 반환
                return ResponseEntity.ok(chatRoomId);
            }
        } catch (Exception e) {
            // 예외 발생 시
            log.error("매칭 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("매칭 중 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelMatch(@RequestParam String account) {
        // 매칭 취소 요청 처리
        matchingService.removeCancelParticipants(account);
        // 매칭 취소 성공 응답
        return ResponseEntity.ok().body("매칭 취소가 성공적으로 이루어졌습니다.");
    }
}
