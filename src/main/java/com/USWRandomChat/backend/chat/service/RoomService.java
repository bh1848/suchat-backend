package com.USWRandomChat.backend.chat.service;

import com.USWRandomChat.backend.chat.repository.MessageRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final Profile profile;
    private final MessageRepository messageRepository;
    private final ProfileRepository profileRepository;

    //퇴장 시 해당 roomId 메시지 삭제
    public void deleteRoomIdMessage(String roomId) {
        messageRepository.deleteByRoomId(roomId);
    }

    //roomId 초기화
    public void exitRoomId(String exitRoomId) {
        String defaultRoomId = "none";
        updateRoomId(exitRoomId, defaultRoomId);
    }

    //roomId 업데이트
    @Transactional
    public void updateRoomId(String currentRoomId, String newRoomId) {
        Optional<Profile> findProfile = profileRepository.findByRoomId(currentRoomId);
        Profile updateProfile = findProfile.orElseThrow(() ->
                new EntityNotFoundException("해당 " +currentRoomId+ "의 회원이 존재하지않습니다."));

        updateProfile.setRoomId(newRoomId);
        profileRepository.save(updateProfile);
    }
}
