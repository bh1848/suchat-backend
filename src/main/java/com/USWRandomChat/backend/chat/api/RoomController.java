package com.USWRandomChat.backend.chat.api;

import com.USWRandomChat.backend.chat.domain.PubMessage;
import com.USWRandomChat.backend.chat.service.RoomService;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ChatException;
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
    public ResponseEntity<String> randomMatch(@RequestHeader("Authorization") String accessToken) {
        try {
            // 매칭 전에 큐에 회원 추가
            roomService.addToMatchingQueue(accessToken);
            // 만료된 참가자 제거
            roomService.removeExpiredParticipants();
            // 매칭 시도하고 채팅방 ID 반환
            String chatRoomId = roomService.performMatching();

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
    public ResponseEntity<String> cancelMatch(@RequestHeader("Authorization") String accessToken) {
        // 매칭 취소 요청 처리
        roomService.removeCancelParticipants(accessToken);
        // 매칭 취소 성공 응답
        return ResponseEntity.ok().body("매칭 취소가 성공적으로 이루어졌습니다.");
    }

    //채팅방 퇴장
    @PatchMapping(value = "/out/{room-id}")
    public ResponseEntity<?> exitRoom(@RequestHeader("Authorization") String accessToken, @PathVariable("room-id") String roomId) {
        try {
            PubMessage exitMessage = new PubMessage(roomId, "system", "채팅이 종료됐습니다.", LocalDateTime.now());
            redisTemplate.convertAndSend(channelTopic.getTopic(), exitMessage);

            roomService.exitRoomId(accessToken, roomId);

            //채팅방에 남은 마지막 한 명이 나갈 때 메시지 삭제
            if (roomService.countRemainingMembers(roomId) == 0) {
                roomService.deleteRoomIdMessage(roomId);
            }

            return ResponseEntity.ok().body("채팅이 종료됐습니다.");
        } catch (ChatException | AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }
}
