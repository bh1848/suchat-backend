package com.USWRandomChat.backend.chat.repository;

import com.USWRandomChat.backend.chat.chatDTO.ChatMessage;
import com.USWRandomChat.backend.chat.domain.Message;
import com.USWRandomChat.backend.profile.domain.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface MessageRepository extends JpaRepository <Message, Long>
{
    Page<ChatMessage> findByRoomId(Pageable pageable, Profile profile);
}
