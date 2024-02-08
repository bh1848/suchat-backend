package com.USWRandomChat.backend.profile.service;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.exception.MemberNotFoundException;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.dto.ProfileResponse;
import com.USWRandomChat.backend.profile.exception.ProfileUpdateException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;

    //프로필 조회
    public ProfileResponse getProfile(String targetMemberId) {

        //Spring Security를 이용하여 현재 로그인한 사용자의 정보를 얻어옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //토큰이 없거나, 익명 사용자인 경우에는 조회 불가
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("프로필 조회 권한이 없습니다.");
        }
        //프로필을 조회할 사용자의 아이디를 검색
        Member member = memberRepository.findByMemberId(targetMemberId)
                .orElseThrow(() -> new MemberNotFoundException("해당 사용자를 찾을 수 없습니다. targetMemberId: " + targetMemberId));
        return new ProfileResponse(member);
    }

    //프로필 업데이트
    public ProfileResponse updateProfile(String memberId, ProfileRequest profileRequest) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberNotFoundException("해당 사용자를 찾을 수 없습니다. memberId: " + memberId));

        try {
            checkNicknameEmpty(profileRequest.getNickname());
            member.setNickname(profileRequest.getNickname());
            member.setMbti(profileRequest.getMbti());
            member.setIntro(profileRequest.getIntro());

            Member updatedMemberProfile = memberRepository.save(member);

            return new ProfileResponse(updatedMemberProfile);
        } catch (IllegalArgumentException e) {
            //닉네임이 비어있을 때 발생하는 예외 처리
            throw new ProfileUpdateException("닉네임을 설정해주세요.");
        } catch (Exception e) {
            //프로필 업데이트 도중 발생하는 예외 처리
            throw new ProfileUpdateException("프로필 업데이트 중 오류가 발생했습니다.");
        }
    }

    //닉네임 빈칸 확인
    private void checkNicknameEmpty(String nickname) {
        if (nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 설정해주세요.");
        }
    }
}