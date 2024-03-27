package com.USWRandomChat.backend.chat.secure.api;

import com.USWRandomChat.backend.chat.domain.PubMessage;
import com.USWRandomChat.backend.chat.secure.service.RoomSecureService;
import com.USWRandomChat.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/match/secure")
@Slf4j
public class RoomSecureController {

    private final RoomSecureService roomSecureService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    @PostMapping("/in")
    public ResponseEntity<ApiResponse> randomMatch(HttpServletRequest request) {
        roomSecureService.addToMatchingQueue(request);
        roomSecureService.removeExpiredParticipants();
        String chatRoomId = roomSecureService.performMatching();
        return ResponseEntity.ok(new ApiResponse("매칭에 성공했습니다.", chatRoomId));
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<ApiResponse> cancelMatch(HttpServletRequest request) {
        roomSecureService.removeCancelParticipants(request);
        return ResponseEntity.ok(new ApiResponse("매칭 취소가 성공적으로 이루어졌습니다."));
    }

    @PatchMapping(value = "/out/{room-id}")
    public ResponseEntity<ApiResponse> exitRoom(HttpServletRequest request, @PathVariable("room-id") String roomId) {
        PubMessage exitMessage = new PubMessage(roomId, "system", "채팅이 종료됐습니다.", LocalDateTime.now());
        redisTemplate.convertAndSend(channelTopic.getTopic(), exitMessage);
        roomSecureService.exitRoomId(request, roomId);
        if (roomSecureService.countRemainingMembers(roomId) == 0) {
            roomSecureService.deleteRoomIdMessage(roomId);
        }
        return ResponseEntity.ok(new ApiResponse("채팅이 종료됐습니다."));
    }
}
