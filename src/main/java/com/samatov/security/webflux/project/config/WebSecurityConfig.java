package com.samatov.security.webflux.project.config;

import com.samatov.security.webflux.project.security.AuthenticationManager;
import com.samatov.security.webflux.project.security.BearerTokenServerAuthenticationConverter;
import com.samatov.security.webflux.project.security.JwtHandler;
import com.samatov.security.webflux.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

    @Value("${jwt.secret}")
    private String secret;

    private final String[] publicRoutes = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-ui/index.html"
    };

    @Bean
    public SecurityWebFilterChain securitySecurityFilterChain(ServerHttpSecurity http, AuthenticationManager authenticationManager) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(publicRoutes).permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint((swe, e) -> {
                            log.error("IN securitySecurityFilterChain - UNAUTHORIZED {} ", e.getMessage());
                            return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
                        })
                        .accessDeniedHandler((swe, e) -> {
                            log.error("IN securitySecurityFilterChain - FORBIDDEN {} ", e.getMessage());
                            return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
                        })
                )
                .addFilterAt(bearerAuthenticationFilter(authenticationManager), SecurityWebFiltersOrder.AUTHORIZATION)
                .build();
    }

    private AuthenticationWebFilter bearerAuthenticationFilter(AuthenticationManager authenticationManager) {
        AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(authenticationManager);
        bearerAuthenticationFilter.setServerAuthenticationConverter(new BearerTokenServerAuthenticationConverter(
                new JwtHandler(secret)));
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
        return bearerAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserService userService) {
        return new AuthenticationManager(userService);
    }
}
