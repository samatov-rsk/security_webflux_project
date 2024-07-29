package com.samatov.security.webflux.project.controller;

import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.mapper.UserMapper;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User Controller", description = "Endpoints for managing users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @Operation(summary = "Get user by ID", description = "Returns the user with the specified ID")
    public Mono<ResponseEntity<UserDTO>> getUserById(@Validated @PathVariable Long id) {
        return userService.getUserById(id)
                .map(userMapper::map)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    @Operation(summary = "Get all users", description = "Returns a list of all users")
    public Mono<ResponseEntity<Flux<UserDTO>>> getAllUsers() {
        return Mono.just(
                ResponseEntity.ok()
                        .body(userService.getAllUser()
                                .map(userMapper::map))
        );
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Create a new user", description = "Creates a new user with the specified details")
    public Mono<ResponseEntity<UserDTO>> createUser(@Validated @RequestBody UserDTO userDTO) {
        User user = userMapper.map(userDTO);
        return userService.registerUser(user)
                .map(userMapper::map)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Update an existing user", description = "Updates an existing user with the specified details")
    public Mono<ResponseEntity<UserDTO>> updateUser(@Validated @RequestBody UserDTO userDTO) {
        User user = userMapper.map(userDTO);
        return userService.updateUser(user).map(userMapper::map)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Delete user by ID", description = "Deletes the user with the specified ID")
    public Mono<ResponseEntity<Void>> deleteUserById(@PathVariable("id") Long id) {
        return userService.getUserById(id)
                .flatMap(user -> userService.deleteUserById(user.getId())
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}
