package com.USWRandomChat.backend.global.security.jwt.repository;

import com.USWRandomChat.backend.global.security.jwt.domain.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JwtRepository extends CrudRepository<Token, Long> {
    Optional<Token> findByAccount(String account);
}
