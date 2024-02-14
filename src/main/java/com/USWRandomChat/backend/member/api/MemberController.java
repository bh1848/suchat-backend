package com.USWRandomChat.backend.member.api;

import com.USWRandomChat.backend.emailAuth.service.EmailService;
import com.USWRandomChat.backend.exception.ExceptionType;
import com.USWRandomChat.backend.exception.errortype.AccountException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.memberDTO.MemberDTO;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignInResponse;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.service.MemberService;

import com.USWRandomChat.backend.response.ListResponse;
import com.USWRandomChat.backend.response.ResponseService;
import com.USWRandomChat.backend.security.jwt.service.JwtService;
import com.USWRandomChat.backend.security.jwt.dto.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.validation.Valid;

import static com.USWRandomChat.backend.exception.ExceptionType.*;


@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ResponseService responseService;
    private final EmailService emailService;
    private final JwtService jwtService;

    //회원가입
    @PostMapping(value = "/sign-up")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) throws MessagingException {
        Member findMember = memberService.signUp(request);
        return new ResponseEntity<>(new SignUpResponse(emailService.createEmailToken(findMember))
                , HttpStatus.OK);
    }

    //로그인
    @PostMapping(value = "/sign-in")
    public ResponseEntity<SignInResponse> signIn(@RequestBody SignInRequest request) {
        return new ResponseEntity<>(memberService.signIn(request), HttpStatus.OK);
    }

    //이메일 인증 확인
    @GetMapping("/confirm-email")
    public ResponseEntity<Boolean> viewConfirmEmail(@Valid @RequestParam String uuid) {
        return new ResponseEntity<>(emailService.verifyEmail(uuid), HttpStatus.OK);
    }

    //이메일 재인증
    @PostMapping("/reconfirm-email")
    public ResponseEntity<String> reconfirmEmail(@RequestParam String uuid) throws MessagingException {
        return new ResponseEntity<>(emailService.recreateEmailToken(uuid), HttpStatus.OK);
    }

    //user인증 확인
//    @GetMapping(value = "/user/get")
//    public ResponseEntity<SignInResponse> getUser(@RequestParam String memberId) throws Exception {
//        return new ResponseEntity<>(memberService.getMember(memberId), HttpStatus.OK);
//    }

    //전체 조회
    @GetMapping("/members")
    public ListResponse<Member> findAll() {
        return responseService.getListResponse(memberService.findAll());
    }

    // memberID 중복 체크
    @PostMapping("/check-duplicate-id")
    public ResponseEntity<Boolean> idCheck(@RequestBody MemberDTO request) {
        return new ResponseEntity<>(memberService.validateDuplicateMemberId(request), HttpStatus.OK);
    }

    // nickname 중복 체크
    @PostMapping("/check-duplicate-nickname")
    public ResponseEntity<Boolean> NicknameCheck(@RequestBody MemberDTO request) {
        return new ResponseEntity<>(memberService.validateDuplicateMemberNickname(request), HttpStatus.OK);
    }

    //자동 로그인 로직: 엑세스 토큰, 리프레시 토큰 재발급
    @PostMapping("/auto-sign-in")
    public ResponseEntity<TokenDto> refresh(@RequestBody TokenDto token) throws Exception {
        return new ResponseEntity<>(jwtService.refreshAccessToken(token), HttpStatus.OK);
    }

    @Data
    @AllArgsConstructor
    static class SignUpResponse {

        private String uuid;
    }

    //임의의 데이터 넣기
    @PostConstruct
    public void init() throws MessagingException {
        Member findMember = memberService.signUp(new SignUpRequest("admin", "password", "cookie_31", "nick"));
        emailService.createEmailToken(findMember);
    }
}