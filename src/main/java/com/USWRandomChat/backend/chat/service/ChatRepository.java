package com.USWRandomChat.backend.chat.service;

import com.USWRandomChat.backend.chat.domain.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRepository {

    //Redis cache keys
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private static final String ENTER_INFO = "ENTER_INFO";

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoom> hashOpsChatRoom;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;

    //전체 조회
    public List<ChatRoom> findAllRoom() {
        return hashOpsChatRoom.values(CHAT_ROOMS);
    }

    //단일 조회
    public ChatRoom findRoomById(String id) {
        return hashOpsChatRoom.get(CHAT_ROOMS, id);
    }

    //채팅방 생성: 서버간 채팅방 공유<- redis hash에 저장
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

    //입장한 채팅방ID와 멤버 세션ID 맵핑 정보 저장
    public void setMemberEnterInfo(String sessionId, String roomId) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
    }

    //멤버 세션으로 입장한 채팅방 ID 조회
    public String getMemberEnterRoomId(String sessionId) {
        return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }

    //멤버 세션정보와 맵핑된 채팅방 ID 삭제
    public void removeMemberEnterInfo(String sessionId) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
    }
}
