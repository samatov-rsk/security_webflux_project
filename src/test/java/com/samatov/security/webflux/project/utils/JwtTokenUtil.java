package com.samatov.security.webflux.project.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenUtil {

    private final SecretKey key;
    private final Integer expiration;
    private final String issuer;

    public JwtTokenUtil(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.expiration}") Integer expiration,
                        @Value("${jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
        this.issuer = issuer;
    }

    public String createToken(Map<String, Object> claims, String subject) {
        Long expirationTimeInMillis = expiration * 1000L;
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTimeInMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createTokenForUser() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "USER");
        claims.put("username", "testuser");
        return createToken(claims, "100");
    }

    public String createTokenForAdmin() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "ADMIN");
        claims.put("username", "admin");
        return createToken(claims, "100");
    }

    public String createTokenForModerator() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "MODERATOR");
        claims.put("username", "moderator");
        return createToken(claims, "100");
    }
}
