package com.samatov.security.webflux.project.config;

import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.security.PBFDK2Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.List;

@TestConfiguration
@Profile("test")
public class TestSecurityConfig {

    @Value("${jwt.password.encoder.secret}")
    private String secret;

    @Value("${jwt.password.encoder.iteration}")
    private Integer iteration;

    @Value("${jwt.password.encoder.keyLength}")
    private Integer keyLength;

    @Bean
    public PBFDK2Encoder passwordEncoder() {
        return new PBFDK2Encoder(secret, iteration, keyLength);
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return authentication -> {
            var user = org.springframework.security.core.userdetails.User.withUsername("testuser")
                    .password("password")
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                    .build();
            Authentication auth = new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities());
            return Mono.just(auth);
        };
    }
}
