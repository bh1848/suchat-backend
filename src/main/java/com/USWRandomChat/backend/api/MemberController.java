package com.USWRandomChat.backend.api;

import com.USWRandomChat.backend.domain.Member;
import com.USWRandomChat.backend.memberDTO.MemberDTO;
import com.USWRandomChat.backend.response.ListResponse;
import com.USWRandomChat.backend.response.ResponseService;
import com.USWRandomChat.backend.response.SingleResponse;
import com.USWRandomChat.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final ResponseService responseService;

    //회원가입
    @PostMapping("/sign-up")
    public SingleResponse<Member> signUp (@RequestBody @Validated MemberDTO request) {
        log.info("MemberController.save");
        log.debug("memberDTO = {}", request);
        try {
            Long id = memberService.save(request);
            Member savedMember = memberService.findById(id);

            return responseService.getSingleResponse(savedMember);
        } catch (RuntimeException e) {
            log.error("회원저장 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    //전체 조회
    @GetMapping("/members")
    public ListResponse<Member> findAll() {
        return responseService.getListResponse(memberService.findAll());
    }

    // memberID 중복 체크
    @PostMapping("/check-duplicate-id")
    public boolean idCheck(@RequestBody MemberDTO request) {
        boolean checkResult = memberService.validateDuplicateMemberId(request);
        if (checkResult == false) {
            //사용가능한 ID
            return true;
        } else {
            //중복
            return false;
        }
    }

    @PostMapping("/check-duplicate-nickname")
    public boolean NicknameCheck(@RequestBody MemberDTO request) {
        boolean checkResult = memberService.validateDuplicateMemberNickname(request);
        if (checkResult == false) {
            //사용가능한 Nickname
            return true;
        } else {
            //중복
            return false;
        }
    }
}
