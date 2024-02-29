package com.USWRandomChat.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionType {
//    100-199: 인증 및 권한
//    200-299: 사용자 관리
//    300-399: 데이터 관리
//    400-499: 토큰 및 인증
//    500-599: 메일 및 통신

    /**
     * Domain: EmailToken
     */
    EMAILTOKEN_IS_EXPIRED("EML-TOK-401", "토큰이 만료되었습니다. 다시 이메일인증 해주세요", BAD_REQUEST),
    EMAIL_NOT_VERIFIED("EML-TOK-402", "이메일 인증되지 않은 회원입니다.", FORBIDDEN),
    Email_Token_Not_Found("EML-TOK-403", "해당 토큰이 존재하지 않습니다.", BAD_REQUEST),

    /**
     * Domain: Member
     */
    USER_NOT_EXISTS("USR-MEM-201", "사용자가 존재하지 않습니다.", BAD_REQUEST),
    ID_OVERLAP("USR-MEM-202", "아이디가 중복됩니다.", INTERNAL_SERVER_ERROR),
    EMAIL_OVERLAP("USR-MEM-203", "이메일이 중복됩니다.", INTERNAL_SERVER_ERROR),
    PASSWORD_ERROR("USR-MEM-204", "비밀번호를 확인해주세요", BAD_REQUEST),
    LOGIN_REQUIRED("USR-MEM-205", "로그인이 필요합니다.", FORBIDDEN),
    BAD_CREDENTIALS("USR-MEM-206", "잘못된 계정 정보입니다.", FORBIDDEN),

    /**
     * Domain : Profile
     */
    PROFILE_NOT_EXISTS("USR-PFL-201", "프로필이 존재하지 않습니다.", BAD_REQUEST),
    NICKNAME_ERROR("USR-PFL-202", "닉네임을 확인해주세요.",BAD_REQUEST),
    NICKNAME_OVERLAP("USR-PFL-203", "닉네임이 중복됩니다.", INTERNAL_SERVER_ERROR),
    NICKNAME_EXPIRATION_TIME("USR-PFL-204", "닉네임 변경 후 30일이 지나야 변경이 가능합니다.", BAD_REQUEST),

    /**
     * Domain : Token
     */
    ACCESS_TOKEN_REQUIRED("TOK-AUTH-401", "엑세스 토큰이 필요합니다", BAD_REQUEST),
    INVALID_ACCESS_TOKEN("TOK-AUTH-402", "잘못된 엑세스 토큰입니다.", BAD_REQUEST),
    REFRESH_TOKEN_EXPIRED("TOK-AUTH-403", "리프레시 토큰 만료", BAD_REQUEST),
    INVALID_REFRESH_TOKEN("TOK-AUTH-404", "잘못된 리프레시 토큰", BAD_REQUEST),

    /**
     * Domain : VerificationCode
     */
    CODE_ERROR("VC-101", "인증번호를 확인해주세요", BAD_REQUEST),
    UUID_NOT_FOUND("VC-102","회원의 uuid를 찾을 수 없습니다.", BAD_REQUEST),
    VERIFICATION_NOT_COMPLETED("VC-103", "인증번호가 확인이 되지 않은 사용자 입니다.", BAD_REQUEST),

    /**
     * Domain : Chat
     */
    ROOM_ID_UPDATE_ERROR("CHT-301", "RoomId 업데이트 실패", BAD_REQUEST),
    MEMBER_COUNT_ERROR("CHT-302","인원 확인이 실패했습니다.",BAD_REQUEST),
    MESSAGE_DELETE_ERROR("CHT-303","메세지가 삭제되지 않았습니다.",BAD_REQUEST),

    /**
     * Domain:ConfirmationToken
     */
    EMAIL_NOT_AUTHED("EML-AUTH-501", "이메일 인증을 받지 않은 사용자 입니다.", UNAUTHORIZED),

    /**
     * 공통
     */
    SEND_MAIL_FAILED("EML-501", "메일 전송에 실패했습니다.", INTERNAL_SERVER_ERROR), //500

    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}