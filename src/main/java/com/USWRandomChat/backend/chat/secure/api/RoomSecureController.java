package com.USWRandomChat.backend.chat.secure.api;

import com.USWRandomChat.backend.chat.domain.PubMessage;
import com.USWRandomChat.backend.chat.secure.service.RoomSecureService;
import com.USWRandomChat.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequiredArgsConstructor
@RequestMapping("/secure/match")
@Slf4j
public class RoomSecureController {

    private final RoomSecureService roomSecureService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    // Stomp를 사용하여 WebSocket 메시지 보내기
    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @PostMapping("/in")
    public ResponseEntity<ApiResponse> randomMatch(HttpServletRequest request) {
        roomSecureService.addToMatchingQueue(request);

        AtomicReference<String> chatRoomId = new AtomicReference<>();

        roomSecureService.performMatchingAsync().thenAccept(result -> {
            chatRoomId.set(result);

            if (result == null) {
                log.info("매칭에 실패했습니다");
                ResponseEntity.ok(new ApiResponse("매칭에 실패했습니다.", null));
            } else {
                log.info("매칭에 성공했습니다");
                ResponseEntity.ok(new ApiResponse("매칭에 성공했습니다.", chatRoomId));
            }
        });

        return ResponseEntity.ok(new ApiResponse("성공적으로 매칭 요청되었습니다.", null));
    }

        @DeleteMapping("/cancel")
    public ResponseEntity<ApiResponse> cancelMatch(HttpServletRequest request) {
        roomSecureService.removeCancelParticipants(request);
        return ResponseEntity.ok(new ApiResponse("매칭 취소가 성공적으로 이루어졌습니다."));
    }

    @PatchMapping(value = "/out/{roomId}")
    public ResponseEntity<ApiResponse> exitRoom(HttpServletRequest request, @PathVariable("roomId") String roomId) {
        PubMessage exitMessage = new PubMessage(roomId, "system", "채팅이 종료됐습니다.", LocalDateTime.now());
        redisTemplate.convertAndSend(channelTopic.getTopic(), exitMessage);
        roomSecureService.exitRoomId(request, roomId);
        if (roomSecureService.countRemainingMembers(roomId) == 0) {
            log.info("Room {} is empty, deleting messages", roomId);
            roomSecureService.deleteRoomIdMessage(roomId);
        }
        return ResponseEntity.ok(new ApiResponse("채팅이 종료됐습니다."));
    }
}
