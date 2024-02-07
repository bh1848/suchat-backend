package com.USWRandomChat.backend.emailAuth.api;

import com.USWRandomChat.backend.emailAuth.exception.VerificationCodeException;
import com.USWRandomChat.backend.emailAuth.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class VerificationCodeController {

    private final VerificationCodeService verificationCodeService;

    // 비밀번호 재설정을 위한 인증번호 생성 및 이메일 전송 API
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestParam String memberId, @RequestParam String email) {
        try {
            verificationCodeService.sendRandomCode(memberId, email);
            return new ResponseEntity<>("인증번호가 전송됐습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("인증번호 전송이 실패했습니다." + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 인증번호 검증
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam String memberId, @RequestParam String verificationCode) {
        boolean isValid = verificationCodeService.verifyCode(memberId, verificationCode);
        if (isValid) {
            return new ResponseEntity<>("인증번호가 확인됐습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("인증번호가 맞지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    //비밀번호 변경
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam String token, @RequestParam String newPassword, @RequestParam String confirmNewPassword) {
        try {
            boolean isUpdated = verificationCodeService.updatePassword(token, newPassword, confirmNewPassword);
            if (isUpdated) {
                return new ResponseEntity<>("비밀번호 변경 성공.", HttpStatus.OK);
            } else {
                // 여기에서 예외 메시지를 적절하게 처리하여 반환
                return new ResponseEntity<>("비밀번호 변경 실패.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (VerificationCodeException e) {
            // 예외 메시지를 포함한 응답 반환
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}