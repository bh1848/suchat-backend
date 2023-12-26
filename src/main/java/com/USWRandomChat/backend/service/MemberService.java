package com.USWRandomChat.backend.service;

import com.USWRandomChat.backend.domain.Member;
import com.USWRandomChat.backend.memberDTO.MemberDTO;
import com.USWRandomChat.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    //쓰기 발생 시 @Transactional 붙이기
    private final MemberRepository memberRepository;

    //회원가입
    @Transactional
    public Long save(MemberDTO signupDTO) {
        Member member = Member.builder()
                .memberId(signupDTO.getMemberId())
                .password(signupDTO.getPassword())
                .email(signupDTO.getEmail())
                .nickname(signupDTO.getNickname())
                .build();
        memberRepository.save(member);
        return member.getId();
    }

    //전체 조회
    public List<Member> findAll(){
        return memberRepository.findAll();
    }

    //id 조회
    public Member findById(Long id){
        Optional<Member> byId = memberRepository.findById(id);
        if (byId.isPresent()) {
            //조회 성공
            return byId.get();
        } else {
            //조회 실패
            return byId.orElse(null);
        }
    }

    //중복 검증 memberId
    public boolean validateDuplicateMemberId(MemberDTO memberDTO) {
        Optional<Member> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        if (byMemberId.isPresent()) {
            //중복
            return true;
        } else {
            //사용가능한 ID
            return false;
        }
    }

    //중복 검증 nickname
    public boolean validateDuplicateMemberNickname(MemberDTO memberDTO) {
        Optional<Member> byNickname = memberRepository.findByNickname(memberDTO.getNickname());
        if (byNickname.isPresent()) {
            //중복
            return true;
        } else {
            //사용가능한 ID
            return false;
        }
    }
}
