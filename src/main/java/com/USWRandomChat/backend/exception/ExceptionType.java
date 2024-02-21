package com.USWRandomChat.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionType {



    /**
     * Domain: EmailToken
     */

    EMAILTOKEN_IS_EXPIRED("EMAILTOKEN001", "토큰이 만료되었습니다. 다시 이메일인증 해주세요", BAD_REQUEST),

    /**
     * Domain: Member
     */
    USER_NOT_EXISTS("USER001", "사용자가 존재하지 않습니다.", BAD_REQUEST),
    LOGIN_ID_OVERLAP("USER002", "아이디가 중복됩니다.", BAD_REQUEST),
    LOGIN_NICKNAME_OVERLAP("USER002", "닉네임이 중복됩니다.", BAD_REQUEST),
    PASSWORD_ERROR("USER003", "비밀번호를 확인해주세요", BAD_REQUEST),
    LOGIN_REQUIRED("USER007", "로그인이 필요합니다.", FORBIDDEN),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "잘못된 계정 정보입니다.", FORBIDDEN),

    /**
     * Domain : Token
     */
    TOKEN_IS_EXPIRED("TOKEN001", "토큰이 만료되었습니다 다시 로그인 해주세요", UNAUTHORIZED),
    INVALID_TOKEN_FORMAT("TOKEN002", "올바른 토큰 형식이 아닙니다", BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
