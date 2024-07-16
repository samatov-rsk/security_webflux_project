package com.samatov.security.webflux.project.security;

import com.samatov.security.webflux.project.exception.AuthException;
import com.samatov.security.webflux.project.model.UserEntity;
import com.samatov.security.webflux.project.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration")
    private Integer expiration;
    @Value("${jwt.issuer")
    private String issuer;

    private TokenDetails generateToken(UserEntity userEntity) {
        Map<String, Object> claims = new HashMap<>(){{
            put("roles", userEntity.getRoles());
            put("username", userEntity.getUsername());
        }};
        return generateToken(claims,userEntity.getId().toString());

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
        return userRepository.findByUsername(username)
                .flatMap(userEntity -> {
                    if (userEntity.getStatus().equals("DELETED")) {
                        return Mono.error(new AuthException("Account disabled", "PROSELYTE ACCOUNT DISABLED"));
                    }
                    if (!passwordEncoder.matches(password, userEntity.getPassword())) {
                        return Mono.error(new BadCredentialsException("INVALID PASSWORD"));
                    }
                    return Mono.just(generateToken(userEntity).toBuilder()
                                    .userId(userEntity.getId())
                            .build());
                })
                .switchIfEmpty(Mono.error(new AuthException("INVALID USERNAME", "PROSELYTE INVALID USERNAME")));

    }
}
