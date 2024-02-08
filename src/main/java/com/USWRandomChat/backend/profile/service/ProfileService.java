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

    public ProfileResponse updateProfile(String memberId, ProfileRequest profileRequest) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberNotFoundException("해당 사용자를 찾을 수 없습니다. memberId: " + memberId));

        try {
            member.setNickname(profileRequest.getNickname());
            member.setMbti(profileRequest.getMbti());
            member.setIntro(profileRequest.getIntro());

            Member updatedMemberProfile = memberRepository.save(member);

            return new ProfileResponse(updatedMemberProfile);
        } catch (EntityNotFoundException e) {
            throw new ProfileUpdateException("프로필 업데이트 중 오류가 발생했습니다.");
        }
    }
}
