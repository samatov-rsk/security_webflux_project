package com.samatov.security.webflux.project.ITController;

import com.samatov.security.webflux.project.config.PostgresTestContainerConfig;
import com.samatov.security.webflux.project.config.TestSecurityConfig;
import com.samatov.security.webflux.project.controller.AuthController;
import com.samatov.security.webflux.project.dto.AuthRequestDTO;
import com.samatov.security.webflux.project.dto.AuthResponseDTO;
import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.errorhandling.AppErrorAttributes;
import com.samatov.security.webflux.project.errorhandling.AppErrorWebExceptionHandler;
import com.samatov.security.webflux.project.mapper.UserMapper;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.repository.UserRepository;
import com.samatov.security.webflux.project.security.SecurityService;
import com.samatov.security.webflux.project.security.TokenDetails;
import com.samatov.security.webflux.project.service.UserService;
import com.samatov.security.webflux.project.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = AuthController.class)
@Import({
        TestSecurityConfig.class,
        SecurityService.class,
        UserService.class,
        UserMapper.class,
        PostgresTestContainerConfig.class,
        AppErrorWebExceptionHandler.class,
        AppErrorAttributes.class
})
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SecurityService securityService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private UserRepository userRepository;

    private User user;
    private UserDTO userDTO;
    private AuthRequestDTO authRequestDTO;
    private AuthResponseDTO authResponseDTO;
    private TokenDetails tokenDetails;

    @BeforeEach
    public void setUp() {
        user = TestUtils.createUser();
        userDTO = TestUtils.createUserDTO();
        authRequestDTO = TestUtils.createAuthRequestDTO();
        authResponseDTO = TestUtils.createAuthResponseDTO();

        tokenDetails = TokenDetails.builder()
                .userId(user.getId())
                .token("dummy-token")
                .issuedAt(new Date())
                .expiresAt(new Date(System.currentTimeMillis() + 3600000))
                .build();
    }

    @Test
    @DisplayName("Регистрация нового пользователя")
    public void testRegisterUser() {
        when(userMapper.map(any(UserDTO.class))).thenReturn(user);
        when(userService.registerUser(any(User.class))).thenReturn(Mono.just(user));
        when(userMapper.map(any(User.class))).thenReturn(userDTO);

        webTestClient.post().uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .isEqualTo(userDTO);
    }

    @Test
    @DisplayName("Аутентификация пользователя")
    public void testLogin() {
        when(securityService.authenticate(any(String.class), any(String.class)))
                .thenReturn(Mono.just(tokenDetails));

        when(userService.findUserByUsername(any(String.class))).thenReturn(Mono.just(user));

        webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDTO.class)
                .isEqualTo(authResponseDTO);
    }

}
