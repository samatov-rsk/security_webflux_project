package com.samatov.security.webflux.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class TestConfig {

    @Bean
    @ConfigurationProperties(prefix = "jwt")
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static class JwtProperties {
        private String secret;
        private Integer expiration;
        private String issuer;


        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Integer getExpiration() {
            return expiration;
        }

        public void setExpiration(Integer expiration) {
            this.expiration = expiration;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
}