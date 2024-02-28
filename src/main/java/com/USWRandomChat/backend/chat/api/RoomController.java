package com.USWRandomChat.backend.chat.api;

import com.USWRandomChat.backend.chat.domain.PubMessage;
import com.USWRandomChat.backend.chat.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matching")
@Slf4j
public class RoomController {

    private final RoomService roomService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    //채팅방 퇴장
    @PatchMapping(value = "/out/{room-id}")
    public void exitRoom(@PathVariable("room-id") String roomId) {
        PubMessage exitMessage = new PubMessage(roomId,
                "system",
                "채팅이 종료됐습니다.",
                LocalDateTime.now());
        redisTemplate.convertAndSend(channelTopic.getTopic(), exitMessage);
        log.info("roomId: {} 채팅 종료", roomId);

        roomService.exitRoomId(roomId);
        log.info("roomId: {} 초기화 완료", roomId);

        //채팅방에 남은 마지막 한명이 나갈 때 메시지 삭제
        if (roomService.countRemainingMembers(roomId) == 0) {
            roomService.deleteRoomIdMessage(roomId);
            log.info("roomId: {}의 메시지 삭제 완료", roomId);
        }
    }
}
