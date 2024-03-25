package com.USWRandomChat.backend.global.security.jwt.api;

import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.global.security.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Slf4j
@RestController
@RequestMapping("/jwt")
@RequiredArgsConstructor
public class JwtController {

    private final JwtService jwtService;

    //토큰 갱신
    @PostMapping("/renew-token")
    public ResponseEntity<ApiResponse> renewToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            TokenDto tokenDto = jwtService.renewToken(request, response);
            ApiResponse apiResponse = new ApiResponse("자동로그인 되었습니다.", tokenDto);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("다시 로그인 해주세요."));
        }
    }
}