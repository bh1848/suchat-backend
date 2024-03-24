package com.USWRandomChat.backend.auth.api;

import com.USWRandomChat.backend.auth.service.AuthService;
import com.USWRandomChat.backend.emailAuth.service.EmailService;
import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.auth.dto.SendVerificationCodeRequest;
import com.USWRandomChat.backend.auth.dto.SendVerificationCodeResponse;
import com.USWRandomChat.backend.auth.dto.UpdatePasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailService emailService;
    private final AuthService authService;

    //임시 회원가입
    @PostMapping(value = "/sign-up")
    public ResponseEntity<ApiResponse> signUp(@RequestBody SignUpRequest request) throws MessagingException {
        MemberTemp findTempMember = authService.signUpMemberTemp(request);
        return new ResponseEntity<>(new ApiResponse(emailService.createEmailToken(findTempMember))
                , HttpStatus.OK);
    }

    //이메일 인증 확인 후 회원가입
    @GetMapping("/confirm-email")
    public ResponseEntity<Boolean> viewConfirmEmail(@Valid @RequestParam String uuid) {
        MemberTemp memberTemp = emailService.findByUuid(uuid);
        authService.signUpMember(memberTemp);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping(value = "/sign-up-finish")
    public ResponseEntity<Boolean> signUpFinish(@RequestParam String uuid) {
        return new ResponseEntity<>(authService.signUpFinish(uuid), HttpStatus.OK);
    }

    //로그인
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse> signIn(@RequestBody SignInRequest request, HttpServletResponse response) {
        try {
            TokenDto tokenDto = authService.signIn(request, response);
            ApiResponse apiResponse = new ApiResponse("로그인 되었습니다.", tokenDto);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("다시 로그인 해주세요."));
        }
    }

    //인증번호 생성 및 전송 요청 처리
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        try {
            String uuid = String.valueOf(authService.sendVerificationCode(request.getAccount(), request.getEmail()));
            SendVerificationCodeResponse response = new SendVerificationCodeResponse(uuid);
            return ResponseEntity.ok(new ApiResponse("인증번호가 전송되었습니다.", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse("인증번호 전송 중 오류가 발생했습니다."));
        }
    }

    //인증번호 확인 요청 처리
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse> verifyCode(@RequestParam String uuid, @RequestParam String verificationCode) {
        boolean isVerified = authService.verifyCode(uuid, verificationCode);
        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse("인증번호가 확인됐습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("인증번호가 맞지 않습니다."));
        }
    }

    //비밀번호 변경 요청 처리
    @PatchMapping("/update-password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestParam String uuid, @RequestBody UpdatePasswordRequest request) {
        try {
            authService.updatePassword(uuid, request.getNewPassword(), request.getConfirmNewPassword());
            return ResponseEntity.ok(new ApiResponse("비밀번호가 변경됐습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("비밀번호 변경에 실패했습니다."));
        }
    }
}