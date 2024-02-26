package com.USWRandomChat.backend.room.api;

import com.USWRandomChat.backend.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/match")
public class RoomController {
    private final RoomService roomService;

    @PostMapping("/in")
    public ResponseEntity<?> randomMatch(@RequestParam String account) {
        try {
            // 매칭 전에 큐에 회원 추가
            roomService.addToMatchingQueue(account);
            // 매칭 시도
            roomService.removeExpiredParticipants();
            String[] matchedMember = roomService.performMatching();
            if (matchedMember != null) {
                return ResponseEntity.status(HttpStatus.OK).body(matchedMember);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("매칭할 회원 수가 부족합니다.1");
            }
        } catch (IllegalArgumentException e) {
            // 매칭할 회원 수가 부족한 경우
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("매칭할 회원 수가 부족합니다.2");
        }
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelMatch(@RequestParam String memberId) {
        roomService.removeCancelParticipants(memberId);
        return ResponseEntity.status(HttpStatus.OK).body("매칭 취소 성공");
    }
}
