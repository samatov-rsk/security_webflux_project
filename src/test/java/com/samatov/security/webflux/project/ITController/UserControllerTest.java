package com.samatov.security.webflux.project.ITController;

import com.samatov.security.webflux.project.config.PostgresTestContainerConfig;
import com.samatov.security.webflux.project.config.TestConfig;
import com.samatov.security.webflux.project.config.TestSecurityConfig;
import com.samatov.security.webflux.project.controller.UserController;
import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.errorhandling.AppErrorAttributes;
import com.samatov.security.webflux.project.errorhandling.AppErrorWebExceptionHandler;
import com.samatov.security.webflux.project.mapper.UserMapper;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.repository.UserRepository;
import com.samatov.security.webflux.project.security.SecurityService;
import com.samatov.security.webflux.project.service.UserService;
import com.samatov.security.webflux.project.utils.JwtTokenUtil;
import com.samatov.security.webflux.project.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = UserController.class)
@Import({TestSecurityConfig.class,
        JwtTokenUtil.class,
        UserMapper.class,
        SecurityService.class,
        UserService.class,
        PostgresTestContainerConfig.class,
        TestConfig.class,
        AppErrorWebExceptionHandler.class,
        AppErrorAttributes.class
})
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    public void setUp() {
        user = TestUtils.createUser();
        userDTO = TestUtils.createUserDTO();
        when(userService.getUserById(any(Long.class))).thenReturn(Mono.just(user));
    }

    @Test
    public void testGetUserById() {
        when(userService.getUserById(any(Long.class))).thenReturn(Mono.just(user));
        when(userMapper.map(any(User.class))).thenReturn(userDTO);

        String token = jwtTokenUtil.createTokenForUser();

        webTestClient.get().uri("/api/v1/user/{id}", 1L)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .isEqualTo(userDTO);
    }

    @Test
    public void testGetAllUsers() {
        when(userService.getAllUser()).thenReturn(Flux.just(user));
        when(userMapper.map(any(User.class))).thenReturn(userDTO);

        String token = jwtTokenUtil.createTokenForModerator();

        webTestClient.get().uri("/api/v1/user/all")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .consumeWith(response -> {
                    List<UserDTO> responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals(1, responseBody.size());
                    assertEquals(userDTO, responseBody.get(0));
                });
    }

    @Test
    public void testCreateUser() {
        when(userMapper.map(any(UserDTO.class))).thenReturn(user);
        when(userService.registerUser(any(User.class))).thenReturn(Mono.just(user));
        when(userMapper.map(any(User.class))).thenReturn(userDTO);

        String token = jwtTokenUtil.createTokenForAdmin();

        webTestClient.post().uri("/api/v1/user/save")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(userDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .isEqualTo(userDTO);
    }

    @Test
    public void testUpdateUser() {
        User user = TestUtils.createUser();
        UserDTO userDTO = TestUtils.createUserDTO();

        User updatedUser = TestUtils.createUpdatedUser(user);
        UserDTO updatedUserDTO = TestUtils.createUpdatedUserDTO(userDTO);

        when(userService.updateUser(any(User.class))).thenReturn(Mono.just(updatedUser));
        when(userMapper.map(any(UserDTO.class))).thenReturn(updatedUser);
        when(userMapper.map(any(User.class))).thenReturn(updatedUserDTO);

        String token = jwtTokenUtil.createTokenForAdmin();

        webTestClient.put().uri("/api/v1/user/update")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(updatedUserDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .isEqualTo(updatedUserDTO);
    }

    @Test
    public void testDeleteUserById_UserExists() {
        when(userService.getUserById(user.getId())).thenReturn(Mono.just(user));
        when(userService.deleteUserById(user.getId())).thenReturn(Mono.empty());

        String token = jwtTokenUtil.createTokenForAdmin();

        webTestClient.delete().uri("/api/v1/user/{id}", user.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void testDeleteUserById_UserNotFound() {
        when(userService.getUserById(user.getId())).thenReturn(Mono.empty());

        String token = jwtTokenUtil.createTokenForAdmin();

        webTestClient.delete().uri("/api/v1/user/{id}", user.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }


}
