package com.samatov.security.webflux.project.controller;

import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.mapper.UserMapper;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.service.UserService;
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
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    public Mono<ResponseEntity<UserDTO>> getUserById(@Validated @PathVariable Long id) {
        return userService.getUserById(id)
                .map(userMapper::map)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public Flux<ResponseEntity<UserDTO>> getAllUsers() {
        return userService.getAllUser()
                .map(user -> userMapper.map(user))
                .map(ResponseEntity::ok);
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public Mono<ResponseEntity<UserDTO>> createUser(@Validated @RequestBody UserDTO userDTO) {
        User user = userMapper.map(userDTO);
        return userService.registerUser(user)
                .map(userMapper::map)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public Mono<ResponseEntity<UserDTO>> updateUser(@Validated @RequestBody UserDTO userDTO) {
        User user = userMapper.map(userDTO);
        return userService.updateUser(user).map(userMapper::map)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public Mono<ResponseEntity<Void>> deleteUserById(@PathVariable("id") Long id) {
        return userService.getUserById(id)
                .flatMap(user -> userService.deleteUserById(user.getId())
                                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}
