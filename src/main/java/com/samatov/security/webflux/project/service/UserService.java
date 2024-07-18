package com.samatov.security.webflux.project.service;

import com.samatov.security.webflux.project.enums.Role;
import com.samatov.security.webflux.project.enums.Status;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> registerUser(User user) {
        return userRepository
                .save(
                        user.toBuilder()
                                .password(passwordEncoder.encode(user.getPassword()))
                                .roles(Role.USER)
                                .status(Status.ACTIVE)
                                .build())
                .doOnSuccess(u -> log.info("IN registerUser - user {} created", u))
                .doOnError(error -> log.error("Error registering user", error));
    }

    public Mono<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
