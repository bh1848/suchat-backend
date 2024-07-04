package com.USWRandomChat.backend.chat.secure.api;

import com.USWRandomChat.backend.chat.secure.service.RoomSecureService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class RoomSecureController {

    private final RoomSecureService roomSecureService;

    // 매칭 요청 처리
    @MessageMapping("/queue/match/in")
    public void handleMatchRequest(Principal principal) {
        //principal 객체에 설정된 acoount 가져옴
        String authenticatedAccount = principal.getName();
        if (authenticatedAccount != null) {
            //인증된 사용자를 큐에 추가
            roomSecureService.addToMatchingQueue(authenticatedAccount);
        }
    }

    // 매칭 취소 처리
    @MessageMapping("/queue/match/cancel")
    public void handleCancelRequest(Principal principal) {
        String authenticatedAccount = principal.getName();
        if (authenticatedAccount != null) {
            roomSecureService.removeCancelParticipants(authenticatedAccount);
        }
    }
}
