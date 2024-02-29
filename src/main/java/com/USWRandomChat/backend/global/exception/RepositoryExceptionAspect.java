package com.USWRandomChat.backend.global.exception;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Aspect
@Component
public class RepositoryExceptionAspect {

    private final SQLExceptionTranslator exTranslator;

    public RepositoryExceptionAspect(DataSource dataSource) {
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }
    @AfterThrowing(
            pointcut = "execution(* com.USWRandomChat.backend.member.repository.*.*(..)) || " +
                       "execution(* com.USWRandomChat.backend.profile.repository.*.*(..)) || " +
                       "execution(* com.USWRandomChat.backend.emailAuth.repository.*.*(..)) || " +
                       "execution(* com.USWRandomChat.backend.chat.repository.*.*(..))",
            throwing = "ex"
    )

    public void logException(JoinPoint joinPoint, Throwable ex) {
        if (ex instanceof SQLException) {
            DataAccessException translatedEx = exTranslator.translate("Repository operation", null, (SQLException) ex);
            throw translatedEx;
        } else {
            // 예외 로깅 등 기타 작업 수행
        }
    }

}
