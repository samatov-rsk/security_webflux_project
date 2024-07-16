package com.samatov.security.webflux.project.security;

import com.samatov.security.webflux.project.exception.UnauthorizedException;
import com.samatov.security.webflux.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final UserRepository userRepository;


    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        return userRepository.findById(principal.getId())
                .filter(userEntity -> userEntity.getStatus().equals("ACTIVE"))
                .switchIfEmpty(Mono.error(new UnauthorizedException("User DISABLED")))
                .map(userEntity -> authentication);
    }
}
