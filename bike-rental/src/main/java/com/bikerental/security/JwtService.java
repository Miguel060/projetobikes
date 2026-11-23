package com.bikerental.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bikerental.model.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMinutes * 60_000;
    }

    public String gerarToken(String username, Role role) {
        long agora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim("role", role.name())
                .issuedAt(new Date(agora))
                .expiration(new Date(agora + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Lança JwtException se o token for inválido ou estiver expirado. */
    public Claims validar(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
