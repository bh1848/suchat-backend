package com.USWRandomChat.backend.chat.api;

import com.USWRandomChat.backend.chat.domain.PubMessage;
import com.USWRandomChat.backend.chat.service.RoomService;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
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
