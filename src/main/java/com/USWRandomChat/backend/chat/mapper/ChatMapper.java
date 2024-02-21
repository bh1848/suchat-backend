package com.USWRandomChat.backend.chat.mapper;

import com.USWRandomChat.backend.chat.dto.MessageRequest;
import com.USWRandomChat.backend.chat.dto.MessageResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    List<MessageResponse> messagesToMessageResponseDtos(List<MessageRequest> messages);
}
