package com.USWRandomChat.backend.chat.secure.api;

import com.USWRandomChat.backend.chat.secure.service.RoomSecureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/open/room")
@RequiredArgsConstructor
public class RoomSecureHttpController {

    private final RoomSecureService roomSecureService;

    // 매칭 요청 처리
    @PostMapping("/match")
    public ResponseEntity<String> handleMatchRequest(@RequestParam String account) {
        roomSecureService.addToMatchingQueue(account);
        return ResponseEntity.ok("{\"message\": \"매칭 요청이 성공적으로 접수되었습니다.\"}");
    }

    // 매칭 취소 처리
    @PostMapping("/cancel")
    public ResponseEntity<String> handleCancelRequest(@RequestParam String account) {
        roomSecureService.removeCancelParticipants(account);
        return ResponseEntity.ok("{\"message\": \"매칭이 성공적으로 취소되었습니다.\"}");
    }

    // 매칭된 사용자 확인 (테스트용)
    @GetMapping("/check")
    public ResponseEntity<String> checkMatchStatus(@RequestParam String account) {
        String roomId = roomSecureService.checkMemberRoomId(account);
        return ResponseEntity.ok("{\"roomId\": \"" + roomId + "\"}");
    }
}