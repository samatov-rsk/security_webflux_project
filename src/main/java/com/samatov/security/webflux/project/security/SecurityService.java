package com.samatov.security.webflux.project.security;

import com.samatov.security.webflux.project.exception.AuthException;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Integer expiration;
    @Value("${jwt.issuer")
    private String issuer;

    private TokenDetails generateToken(User user) {
        Map<String, Object> claims = new HashMap<>(){{
            put("roles", user.getRoles());
            put("username", user.getUsername());
        }};
        return generateToken(claims,user.getId().toString());

    }
    private TokenDetails generateToken(Map<String, Object> claims, String subject) {
        Long expirationTimeInMillis = expiration * 1000L;
        Date expirationDate = new Date(new Date().getTime() + expirationTimeInMillis);

        return generationToken(expirationDate, claims, subject);
    }

    private TokenDetails generationToken(Date expiration, Map<String, Object> claims, String subject) {
        Date createdDate = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setId(UUID.randomUUID().toString())
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.getBytes()))
                .compact();

        return TokenDetails.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expiration)
                .build();
    }

    public Mono<TokenDetails> authenticate(String username, String password) {
        return userService.findUserByUsername(username)
                .flatMap(user -> {
                    if (user.getStatus().equals("DELETED")) {
                        return Mono.error(new AuthException("Account disabled", "PROSELYTE ACCOUNT DISABLED"));
                    }
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new BadCredentialsException("INVALID PASSWORD"));
                    }
                    return Mono.just(generateToken(user).toBuilder()
                                    .userId(user.getId())
                            .build());
                })
                .switchIfEmpty(Mono.error(new AuthException("INVALID USERNAME", "PROSELYTE INVALID USERNAME")));

    }
}
