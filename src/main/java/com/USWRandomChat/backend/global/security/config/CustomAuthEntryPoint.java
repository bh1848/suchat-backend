package com.USWRandomChat.backend.global.security.config;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccessTokenException;
import com.USWRandomChat.backend.global.exception.errortype.RefreshTokenException;
import com.USWRandomChat.backend.global.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        int status;
        String message;
        String code;

        if (authException.getCause() instanceof RefreshTokenException) {
            status = HttpServletResponse.SC_FORBIDDEN;
            message = "리프레시 토큰 만료";
            code = "REFRESH_TOKEN_EXPIRED";
        } else {
            status = HttpServletResponse.SC_UNAUTHORIZED;
            message = "엑세스 토큰 만료";
            code = "ACCESS_TOKEN_EXPIRED";
        }

        response.setStatus(status);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exception(authException.getClass().getSimpleName())
                .code(code)
                .message(message)
                .status(status)
                .error(response.getStatus() == 401 ? "Unauthorized" : "Forbidden")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
