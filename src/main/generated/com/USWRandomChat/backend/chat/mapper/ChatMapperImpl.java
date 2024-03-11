package com.USWRandomChat.backend.chat.mapper;

import com.USWRandomChat.backend.chat.dto.MessageRequest;
import com.USWRandomChat.backend.chat.dto.MessageResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-03-08T13:38:17+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)"
)
@Component
public class ChatMapperImpl implements ChatMapper {

    @Override
    public List<MessageResponse> messagesToMessageResponseDtos(List<MessageRequest> messages) {
        if ( messages == null ) {
            return null;
        }

        List<MessageResponse> list = new ArrayList<MessageResponse>( messages.size() );
        for ( MessageRequest messageRequest : messages ) {
            list.add( messageRequestToMessageResponse( messageRequest ) );
        }

        return list;
    }

    protected MessageResponse messageRequestToMessageResponse(MessageRequest messageRequest) {
        if ( messageRequest == null ) {
            return null;
        }

        MessageResponse.MessageResponseBuilder messageResponse = MessageResponse.builder();

        messageResponse.sender( messageRequest.getSender() );
        messageResponse.contents( messageRequest.getContents() );

        return messageResponse.build();
    }
}
