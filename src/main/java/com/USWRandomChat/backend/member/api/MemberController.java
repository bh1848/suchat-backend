package com.USWRandomChat.backend.member.api;

import com.USWRandomChat.backend.emailAuth.service.EmailService;
import com.USWRandomChat.backend.global.exception.errortype.MailException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.memberDTO.MemberDTO;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignInResponse;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.service.FindIdService;
import com.USWRandomChat.backend.member.service.MemberService;
import com.USWRandomChat.backend.global.response.ListResponse;
import com.USWRandomChat.backend.global.response.ResponseService;
import com.USWRandomChat.backend.global.security.jwt.service.JwtService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ResponseService responseService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final FindIdService findIdService;

    //회원가입
    @PostMapping(value = "/sign-up")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) throws MessagingException {
        Member findMember = memberService.signUp(request);
        return new ResponseEntity<>(new SignUpResponse(emailService.createEmailToken(findMember))
                , HttpStatus.OK);
    }

    @GetMapping(value = "/sign-up-finish")
    public ResponseEntity<Boolean> signUpFinish(@RequestParam String uuid) {
        return new ResponseEntity<>(memberService.signUpFinish(uuid), HttpStatus.OK);
    }

    //로그인
    @PostMapping(value = "/sign-in")
    public ResponseEntity<SignInResponse> signIn(@RequestBody SignInRequest request) {
        return new ResponseEntity<>(memberService.signIn(request), HttpStatus.OK);
    }

    //자동 로그인 로직: 엑세스 토큰, 리프레시 토큰 재발급
    @PostMapping("/auto-sign-in")
    public ResponseEntity<String> refresh(@RequestHeader("Authorization") String accessToken) throws Exception {
        return new ResponseEntity<>(jwtService.refreshAccessToken(accessToken), HttpStatus.OK);
    }

    //로그아웃
    @PostMapping("/sign-out")
    public ResponseEntity<String> signOut(@RequestHeader("Authorization") String accessToken) throws Exception {
        jwtService.signOut(accessToken);
        return ResponseEntity.ok("로그아웃 성공");
    }

    //회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestHeader("Authorization") String accessToken) {
        memberService.withdraw(accessToken);
        return new ResponseEntity<>("회원 탈퇴 성공", HttpStatus.OK);
    }

    //이메일 인증 확인
    @GetMapping("/confirm-email")
    public ResponseEntity<Boolean> viewConfirmEmail(@Valid @RequestParam String uuid) {
        return new ResponseEntity<>(emailService.verifyEmail(uuid), HttpStatus.OK);
    }

    //이메일 재인증
    @PostMapping("/reconfirm-email")
    public ResponseEntity<String> reconfirmEmail(@Valid @RequestParam String uuid) throws MessagingException {
        return new ResponseEntity<>(emailService.recreateEmailToken(uuid), HttpStatus.OK);
    }

    //전체 조회
    @GetMapping("/members")
    public ListResponse<Member> findAll() {
        return responseService.getListResponse(memberService.findAll());
    }

  //account 중복 체크
    @PostMapping("/check-duplicate-id-signUp")
    public ResponseEntity<String> idCheck(@RequestBody MemberDTO request) {
        memberService.validateDuplicateAccount(request);
        return new ResponseEntity<>("사용 가능한 닉네임입니다.", HttpStatus.OK);
    }

    //이메일 중복 확인
    @PostMapping("/check-duplicate-email")
    public ResponseEntity<String> checkDuplicateEmail(@RequestBody MemberDTO memberDTO) {
        try {
            memberService.checkDuplicateEmail(memberDTO);
            return new ResponseEntity<>("사용 가능한 이메일입니다.", HttpStatus.OK);
        } catch (MailException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    //회원가입 시의 닉네임 확인
    @PostMapping("/check-duplicate-nickname-signUp")
    public ResponseEntity<String> signUp(@RequestBody MemberDTO memberDTO) {
        memberService.checkDuplicateNicknameSignUp(memberDTO);
        return new ResponseEntity<>("사용 가능한 닉네임입니다.", HttpStatus.CREATED);
    }

    //이미 가입된 사용자의 닉네임 중복 확인
    @PostMapping("/check-duplicate-nickname")
    public ResponseEntity<String> checkDuplicateNickname(@RequestParam String accessToken, @RequestBody MemberDTO memberDTO) {
        memberService.checkDuplicateNickname(accessToken, memberDTO);
        return new ResponseEntity<>("사용 가능한 닉네임입니다.", HttpStatus.OK);
    }

    //Id 찾기 로직: 이메일 인증된 회원만
    @PostMapping(value = "/find-Id")
    public ResponseEntity<Boolean> findId(@RequestParam String email) throws MessagingException {
        return new ResponseEntity<>(findIdService.findById(email), HttpStatus.OK);
    }

    @Data
    @AllArgsConstructor
    static class SignUpResponse {

        private String uuid;
    }
}