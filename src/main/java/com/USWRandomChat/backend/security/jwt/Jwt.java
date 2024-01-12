package com.USWRandomChat.backend.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "refresh_token")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Jwt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "expiration")
    private Long expiration;

    @Column(name = "refresh_token_expiration")
    private Long refreshTokenExpiration;

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}

