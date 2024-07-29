package com.samatov.security.webflux.project.controller;

import com.samatov.security.webflux.project.dto.AuthRequestDTO;
import com.samatov.security.webflux.project.dto.AuthResponseDTO;
import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.mapper.UserMapper;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.security.CustomPrincipal;
import com.samatov.security.webflux.project.security.SecurityService;
import com.samatov.security.webflux.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Controller", description = "Endpoints for user authentication")
public class AuthController {

    private final SecurityService securityService;
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with the given details")
    public Mono<UserDTO> register(@RequestBody UserDTO userDTO) {
        User user = userMapper.map(userDTO);
        return userService.registerUser(user)
                .map(userMapper::map);
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Authenticates a user and returns a JWT token")
    public Mono<AuthResponseDTO> login(@RequestBody AuthRequestDTO authDTO) {
        return securityService.authenticate(authDTO.getUsername(), authDTO.getPassword())
                .flatMap(tokenDetails -> Mono.just(
                        AuthResponseDTO.builder()
                                .userId(tokenDetails.getUserId())
                                .token(tokenDetails.getToken())
                                .issuerDate(tokenDetails.getIssuedAt())
                                .expiresDate(tokenDetails.getExpiresAt())
                                .build()
                ));
    }

    @GetMapping("/info")
    @Operation(summary = "Get user info", description = "Returns the information of the authenticated user")
    public Mono<UserDTO> getUserInfo(Authentication authentication) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();

        return userService.getUserById(principal.getId())
                .flatMap(userEntity -> Mono.just(userMapper.map(userEntity)));
    }
}
