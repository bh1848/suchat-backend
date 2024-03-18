package com.USWRandomChat.backend.chat.api;

import com.USWRandomChat.backend.chat.domain.PubMessage;
import com.USWRandomChat.backend.chat.service.RoomService;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ChatException;
import com.USWRandomChat.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/match")
@Slf4j
public class RoomController {

    private final RoomService roomService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    @PostMapping("/in")
    public ResponseEntity<ApiResponse> randomMatch(@RequestHeader("Authorization") String accessToken) {
        try {
            roomService.addToMatchingQueue(accessToken);
            roomService.removeExpiredParticipants();
            String chatRoomId = roomService.performMatching();

            if (chatRoomId == null) {
                throw new ChatException(ExceptionType.NOT_ENOUGH_MEMBER);
            } else {
                return ResponseEntity.ok(new ApiResponse("매칭애 성공했습니다.", chatRoomId));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("매칭에 실패했습니다."));
        }
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<ApiResponse> cancelMatch(@RequestHeader("Authorization") String accessToken) {
        roomService.removeCancelParticipants(accessToken);
        return ResponseEntity.ok(new ApiResponse("매칭 취소가 성공적으로 이루어졌습니다."));
    }

    @PatchMapping(value = "/out/{room-id}")
    public ResponseEntity<ApiResponse> exitRoom(@RequestHeader("Authorization") String accessToken, @PathVariable("room-id") String roomId) {
        try {
            PubMessage exitMessage = new PubMessage(roomId, "system", "채팅이 종료됐습니다.", LocalDateTime.now());
            redisTemplate.convertAndSend(channelTopic.getTopic(), exitMessage);

            roomService.exitRoomId(accessToken, roomId);

            if (roomService.countRemainingMembers(roomId) == 0) {
                roomService.deleteRoomIdMessage(roomId);
            }

            return ResponseEntity.ok(new ApiResponse("채팅이 종료됐습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("채팅 종료에 실패했습니다."));
        }
    }
}
