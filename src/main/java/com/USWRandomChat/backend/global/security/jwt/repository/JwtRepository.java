package com.USWRandomChat.backend.global.security.jwt.repository;

import com.USWRandomChat.backend.global.security.jwt.domain.Token;
import org.springframework.data.repository.CrudRepository;

public interface JwtRepository extends CrudRepository<Token, String> {
}
