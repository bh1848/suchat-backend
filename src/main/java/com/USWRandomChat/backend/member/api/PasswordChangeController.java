package com.USWRandomChat.backend.member.api;

import com.USWRandomChat.backend.exception.errortype.AccountException;
import com.USWRandomChat.backend.exception.errortype.CodeException;
import com.USWRandomChat.backend.member.memberDTO.PasswordChangeRequest;
import com.USWRandomChat.backend.member.memberDTO.PasswordChangeResponse;
import com.USWRandomChat.backend.member.memberDTO.SendRandomCodeRequest;
import com.USWRandomChat.backend.member.memberDTO.SendRandomCodeResponse;
import com.USWRandomChat.backend.member.service.PasswordChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final PasswordChangeService passwordChangeService;

    //비밀번호 재설정을 위한 인증번호 생성 및 이메일 전송 API
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestHeader("Authorization") String accessToken, @RequestBody SendRandomCodeRequest sendRandomCodeRequest) {
        try {
            SendRandomCodeResponse response = passwordChangeService.sendRandomCode(accessToken, sendRandomCodeRequest);
            return ResponseEntity.ok("인증번호가 전송됐습니다. account: " + response.getAccount() + ", email: " + response.getEmail());
        } catch (AccountException | CodeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("요청 처리 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    //인증번호 검증
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestHeader("Authorization") String accessToken, @RequestParam String verificationCode) {
        try {
            boolean isValid = passwordChangeService.verifyRandomCode(accessToken, verificationCode);
            if (isValid) {
                return ResponseEntity.ok("인증번호가 확인됐습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 맞지 않습니다.");
            }
        } catch (CodeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패: " + e.getMessage());
        }
    }

    //비밀번호 변경
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestHeader("Authorization") String accessToken, @RequestBody PasswordChangeRequest passwordChangeRequest) {
        try {
            PasswordChangeResponse response = passwordChangeService.updatePassword(accessToken, passwordChangeRequest);
            return ResponseEntity.ok("비밀번호가 변경되었습니다. account: " + response.getAccount());
        } catch (AccountException | CodeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호 변경 중 오류가 발생했습니다. " + e.getMessage());
        }
    }
}