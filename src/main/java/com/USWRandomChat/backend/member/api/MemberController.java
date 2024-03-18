package com.USWRandomChat.backend.member.api;

import com.USWRandomChat.backend.emailAuth.service.EmailService;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.ApiResponse;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenResponse;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.domain.MemberTemp;
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
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private static final long NICKNAME_CHANGE_LIMIT_DAYS = 30;
    private final MemberService memberService;
    private final ResponseService responseService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final FindIdService findIdService;

    //임시 회원가입
    @PostMapping(value = "/sign-up")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) throws MessagingException {
        MemberTemp findTempMember = memberService.signUpMemberTemp(request);
        return new ResponseEntity<>(new SignUpResponse(emailService.createEmailToken(findTempMember))
                , HttpStatus.OK);
    }

    //이메일 인증 확인 후 회원가입
    @GetMapping("/confirm-email")
    public ResponseEntity<Boolean> viewConfirmEmail(@Valid @RequestParam String uuid) {
        MemberTemp memberTemp = emailService.findByUuid(uuid);
        memberService.signUpMember(memberTemp);
        return new ResponseEntity<>(true, HttpStatus.OK);
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
    public ResponseEntity<TokenResponse> refresh(@RequestHeader("Authorization") String accessToken) {
        try {
            TokenResponse tokenResponse = jwtService.refreshAccessToken(accessToken);
            return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        } catch (AccountException e) {
            throw new AccountException(ExceptionType.SIGN_IN_REQUIRED);
        }
    }

    //로그아웃
    @PostMapping("/sign-out")
    public ResponseEntity<ApiResponse> signOut(@RequestHeader("Authorization") String accessToken) throws Exception {
        try {
            jwtService.signOut(accessToken);
            return ResponseEntity.ok(new ApiResponse("로그아웃 되었습니다."));
        } catch (AccountException e) {
            throw new AccountException(ExceptionType.SIGN_OUT_FAIL);
        }
    }

    //회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse> withdraw(@RequestHeader("Authorization") String accessToken) {
        try {
            memberService.withdraw(accessToken);
            return ResponseEntity.ok(new ApiResponse("회원 탈퇴가 완료됐습니다."));
        } catch (AccountException e) {
            throw new AccountException(ExceptionType.WITH_DRAW_FAIL);
        }
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

    //계정 중복 체크
    @PostMapping("/check-duplicate-account")
    public ResponseEntity<ApiResponse> checkDuplicateAccount(@RequestBody MemberDTO request) {
        try {
            memberService.checkDuplicateAccount(request);
            return ResponseEntity.ok(new ApiResponse("사용 가능한 계정입니다."));
        } catch (AccountException e) {
            throw new AccountException(ExceptionType.ACCOUNT_OVERLAP);
        }
    }

    //이메일 중복 확인
    @PostMapping("/check-duplicate-email")
    public ResponseEntity<ApiResponse> checkDuplicateEmail(@RequestBody MemberDTO memberDTO) {
        try {
            memberService.checkDuplicateEmail(memberDTO);
            return ResponseEntity.ok(new ApiResponse("사용 가능한 이메일입니다."));
        } catch (AccountException e) {
            throw new AccountException(ExceptionType.EMAIL_OVERLAP);
        }
    }

    //회원가입 시의 닉네임 확인
    @PostMapping("/check-duplicate-nickname-signUp")
    public ResponseEntity<ApiResponse> checkDuplicateNicknameSignUp(@RequestBody MemberDTO memberDTO) {
        try {
            memberService.checkDuplicateNicknameSignUp(memberDTO);
            return ResponseEntity.ok(new ApiResponse("사용 가능한 닉네임입니다."));
        } catch (AccountException e) {
            throw new ProfileException(ExceptionType.NICKNAME_OVERLAP);
        }
    }

//    //이미 가입된 사용자의 닉네임 중복 확인
//    @PostMapping("/check-duplicate-nickname")
//    public ResponseEntity<ApiResponse> checkDuplicateNickname(@RequestHeader("Authorization") String accessToken, @RequestBody MemberDTO memberDTO) {
//        try {
//            memberService.checkDuplicateNickname(accessToken, memberDTO);
//            return ResponseEntity.ok(new ApiResponse(true, "사용 가능한 닉네임입니다."));
//        } catch (AccountException e) {
//            if (e.getType() == ExceptionType.NICKNAME_EXPIRATION_TIME) { // ExceptionType 열거형을 사용하여 비교
//                // 닉네임 변경 가능 날짜 계산
//                LocalDateTime canChangeAfter = LocalDateTime.now().plusDays(NICKNAME_CHANGE_LIMIT_DAYS); // NICKNAME_CHANGE_LIMIT_DAYS 상수 사용
//                return ResponseEntity
//                        .status(HttpStatus.CONFLICT)
//                        .body(new ApiResponse(false, "닉네임을 변경한 지 30일이 지나지 않았습니다.", Map.of("canChangeAfter", canChangeAfter)));
//            } else {
//                return ResponseEntity
//                        .status(HttpStatus.CONFLICT)
//                        .body(new ApiResponse(false, "이미 사용 중인 닉네임입니다."));
//            }
//        }
//    }

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