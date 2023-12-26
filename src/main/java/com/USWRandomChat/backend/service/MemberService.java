package com.USWRandomChat.backend.service;

import com.USWRandomChat.backend.domain.Member;
import com.USWRandomChat.backend.memberDTO.MemberDTO;
import com.USWRandomChat.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    //회원가입
    public void save(MemberDTO signupDTO) {
        Member member = Member.builder()
                .memberId(signupDTO.getMemberId())
                .password(signupDTO.getPassword())
                .email(signupDTO.getEmail())
                .nickname(signupDTO.getNickname())
                .build();
        memberRepository.save(member);
    }

    //전체 조회
    public List<Member> findAll(){
        return memberRepository.findAll();
    }

    //id 조회
    public Member findById(Long id){
        Optional<Member> memberId = memberRepository.findById(id);
        if (memberId.isPresent()) {
            //조회 성공
            return memberId.get();
        } else {
            //조회 실패
            return memberId.orElse(null);
        }
    }

    //중복 검증 memberId
    public boolean validateDuplicateMemberId(MemberDTO memberDTO) {
        Optional<Member> findMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        if (findMemberId.isPresent()) {
            //중복
            return true;
        } else {
            //사용가능한 ID
            return false;
        }
    }

    //중복 검증 memberNickname
    public boolean validateDuplicateMemberNickname(MemberDTO memberDTO) {
        Optional<Member> findNickname = memberRepository.findByNickname(memberDTO.getNickname());
        if (findNickname.isPresent()) {
            //중복
            return true;
        } else {
            //사용가능한 ID
            return false;
        }
    }
}
