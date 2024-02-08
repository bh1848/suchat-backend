package com.USWRandomChat.backend.profile.service;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.exception.MemberNotFoundException;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.dto.ProfileResponse;
import com.USWRandomChat.backend.profile.exception.ProfileUpdateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;

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
            // 닉네임이 비어있을 때 발생하는 예외 처리
            throw new ProfileUpdateException("닉네임을 설정해주세요.");
        } catch (Exception e) {
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