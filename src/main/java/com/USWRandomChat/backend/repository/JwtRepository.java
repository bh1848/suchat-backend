package com.USWRandomChat.backend.repository;

import com.USWRandomChat.backend.security.jwt.Jwt;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JwtRepository extends CrudRepository<Jwt, Long> {
}
