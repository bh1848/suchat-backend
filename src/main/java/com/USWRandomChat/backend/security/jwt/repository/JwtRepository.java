package com.USWRandomChat.backend.security.jwt.repository;

import com.USWRandomChat.backend.security.jwt.domain.Token;
import org.springframework.data.repository.CrudRepository;

public interface JwtRepository extends CrudRepository<Token, Long> {
}
