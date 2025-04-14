package com.instant.message.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET;
    private Algorithm ALGORITHM;
    private JWTVerifier VERIFIER;
    @Value("${jwt.expirationMs}")
    private String EXPIRATION_MS;

    public String generateToken(String userId) {
        return JWT.create()
                .withSubject(userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + Long.parseLong(EXPIRATION_MS)))
                .sign(ALGORITHM);
    }

    public String validateTokenAndGetUserId(String token) throws JWTVerificationException {
        DecodedJWT jwt = VERIFIER.verify(token);
        return jwt.getSubject();
    }
}