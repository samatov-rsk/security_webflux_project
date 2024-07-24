package com.samatov.security.webflux.project.service;

import com.samatov.security.webflux.project.exception.NotFoundException;
import com.samatov.security.webflux.project.exception.UserException;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Когда getUserById вызывается, то успешный результат")
    void getUserByIdTest() {
        User user = new User();
        when(userRepository.findById(anyLong())).thenReturn(Mono.just(user));

        Mono<User> result = userService.getUserById(1L);

        assertEquals(user, result.block());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Когда getUserById вызывается, то NotFoundException")
    void getUserByIdNotFoundTest() {
        when(userRepository.findById(anyLong())).thenReturn(Mono.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(1L).block());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Когда getAllUser вызывается, то успешный результат")
    void getAllUserTest() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        List<User> userList = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(Flux.fromIterable(userList));

        Flux<User> result = userService.getAllUser();

        StepVerifier.create(result)
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Когда getAllUser вызывается, то UserException")
    void getAllUserNotFoundTest() {
        when(userRepository.findAll()).thenReturn(Flux.empty());

        assertThrows(UserException.class, () -> userService.getAllUser().collectList().block());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Когда updateUser вызывается, то успешный результат")
    void updateUserTest() {
        User user = new User();
        user.setId(1L);
        user.setPassword("newPassword");
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPassword("oldPassword");

        when(userRepository.findById(anyLong())).thenReturn(Mono.just(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

        Mono<User> result = userService.updateUser(user);

        assertEquals(user, result.block());
        verify(userRepository, times(1)).findById(anyLong());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Когда updateUser вызывается, то NotFoundException")
    void updateUserNotFoundTest() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(anyLong())).thenReturn(Mono.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(user).block());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Когда deleteUserById вызывается, то успешный результат")
    void deleteUserByIdTest() {
        User user = new User();
        when(userRepository.findById(anyLong())).thenReturn(Mono.just(user));
        when(userRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        Mono<Void> result = userService.deleteUserById(1L);

        assertNull(result.block());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).deleteById(anyLong());
    }

    @Test
    @DisplayName("Когда deleteUserById вызывается, то NotFoundException")
    void deleteUserByIdNotFoundTest() {
        when(userRepository.findById(anyLong())).thenReturn(Mono.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUserById(1L).block());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Когда registerUser вызывается, то успешный результат")
    void registerUserTest() {
        User user = new User();
        user.setPassword("password");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

        Mono<User> result = userService.registerUser(user);

        assertEquals(user, result.block());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Когда findUserByUsername вызывается, то успешный результат")
    void findUserByUsernameTest() {
        User user = new User();
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(user));

        Mono<User> result = userService.findUserByUsername("username");

        assertEquals(user, result.block());
        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    @DisplayName("Когда findUserByUsername вызывается, то UserException")
    void findUserByUsernameNotFoundTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        assertThrows(UserException.class, () -> userService.findUserByUsername("username").block());
        verify(userRepository, times(1)).findByUsername(anyString());
    }
}
