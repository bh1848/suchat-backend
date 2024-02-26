package com.USWRandomChat.backend.security.jwt.repository;

import com.USWRandomChat.backend.security.jwt.domain.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JwtRepository extends CrudRepository<Token, Long> {
    Optional<Token> findByAccount(String account);
}
