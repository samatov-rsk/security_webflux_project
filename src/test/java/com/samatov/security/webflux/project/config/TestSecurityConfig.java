package com.samatov.security.webflux.project.config;

import com.samatov.security.webflux.project.security.PBFDK2Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

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
}
