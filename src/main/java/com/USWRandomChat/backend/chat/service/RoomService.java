package com.USWRandomChat.backend.chat.service;

import com.USWRandomChat.backend.chat.repository.MessageRepository;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ChatException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final MessageRepository messageRepository;
    private final ProfileRepository profileRepository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    //해당 roomId의 모든 메시지를 삭제
    public void deleteRoomIdMessage(String roomId) {
        try {
            messageRepository.deleteByRoomId(roomId);
            log.info("Room ID {}에 대한 메시지가 성공적으로 삭제되었습니다.", roomId);
        } catch (Exception e) {
            log.error("Room ID {} 메시지 삭제 중 오류 발생: {}", roomId, e.getMessage());
            throw new ChatException(ExceptionType.MESSAGE_DELETE_ERROR);
        }
    }

    //방에 남아 있는 인원 수를 확인
    public int countRemainingMembers(String roomId) {
        try {
            List<Profile> remainingMembers = profileRepository.findAllProfilesByRoomId(roomId);
            return remainingMembers.size();
        } catch (Exception e) {
            log.error("Room ID {}에 대한 남은 인원 수 확인 중 오류 발생: {}", roomId, e.getMessage());
            throw new ChatException(ExceptionType.MEMBER_COUNT_ERROR);
        }
    }

    //특정 사용자의 roomId를 'none'으로 초기화
    public void exitRoomId(String accessToken, String exitRoomId) {
        try {
            Member member = validateAccessTokenAndGetMember(accessToken);
            updateProfileRoomIdToNone(member, exitRoomId);
        } catch (AccountException | ChatException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Room ID {} 업데이트 중 예외 발생: {}", exitRoomId, e.getMessage());
            throw new ChatException(ExceptionType.ROOM_ID_UPDATE_ERROR);
        }
    }

    //액세스 토큰을 검증하고, 해당하는 Member 객체를 반환
    private Member validateAccessTokenAndGetMember(String accessToken) {
        //엑세스 토큰의 유효성 검증
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        String account = jwtProvider.getAccount(accessToken);
        return memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
    }

    //특정 Member의 roomId를 'none'으로 업데이트
    private void updateProfileRoomIdToNone(Member member, String exitRoomId) {
        Optional<Profile> profileOpt = profileRepository.findByMemberAndRoomId(member, exitRoomId);

        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            profile.setRoomId("none");
            profileRepository.save(profile);
            log.info("계정 {}의 Room ID가 'none'으로 성공적으로 설정되었습니다.", member.getAccount());
        } else {
            throw new ProfileException(ExceptionType.PROFILE_NOT_EXISTS);
        }
    }
}