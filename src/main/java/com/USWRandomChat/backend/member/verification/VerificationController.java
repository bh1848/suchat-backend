package com.USWRandomChat.backend.member.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService authenticationService;

    // 인증번호 전송 요청 처리
    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody SendCodeRequest request) {
        authenticationService.sendVerificationCode(request.getAccount(), request.getEmail());
        return ResponseEntity.ok().build();
    }

    // 인증번호 확인 요청 처리
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody SendCodeRequest request) {
        boolean isVerified = authenticationService.verifyCode(request.getAccount(), request.getCode());
        if (isVerified) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
