package com.samatov.security.webflux.project.ITController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samatov.security.webflux.project.config.PostgresTestContainerConfig;
import com.samatov.security.webflux.project.config.TestSecurityConfig;
import com.samatov.security.webflux.project.dto.AuthRequestDTO;
import com.samatov.security.webflux.project.dto.AuthResponseDTO;
import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.enums.Role;
import com.samatov.security.webflux.project.enums.Status;
import com.samatov.security.webflux.project.mapper.UserMapper;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.repository.EventRepository;
import com.samatov.security.webflux.project.repository.UserRepository;
import com.samatov.security.webflux.project.security.SecurityService;
import com.samatov.security.webflux.project.service.UserService;
import com.samatov.security.webflux.project.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainerConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Testcontainers
public class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private UserDTO userDTO;
    private AuthRequestDTO authRequestDTO;
    private AuthResponseDTO authResponseDTO;

    @BeforeEach
    public void setUp() {
        user = TestUtils.createUser();
        userDTO = TestUtils.createUserDTO();
        authRequestDTO = TestUtils.createAuthRequestDTO();
        authResponseDTO = TestUtils.createAuthResponseDTO();

        eventRepository.deleteAll()
                .then(userRepository.deleteAll())
                .block();
    }

    @Test
    @DisplayName("Регистрация нового пользователя")
    public void testRegisterUser() {
        UserDTO registerRequest = userDTO;
        System.out.println("Register request: " + registerRequest);

        webTestClient.post().uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .value(response -> {
                    assert response.getUsername().equals(registerRequest.getUsername());
                    assert response.getRoles().equals(Role.USER);
                    assert response.getStatus().equals(Status.ACTIVE);
                });

        User savedUser = userRepository.findByUsername(registerRequest.getUsername()).block();
        assert savedUser != null;
        assert savedUser.getUsername().equals(registerRequest.getUsername());
        assert savedUser.getRoles().equals(Role.USER);
        assert savedUser.getStatus().equals(Status.ACTIVE);
    }

    @Test
    @DisplayName("Аутентификация пользователя")
    public void testLogin() {
        userService.registerUser(user).block();

        AuthRequestDTO loginRequest = AuthRequestDTO.builder()
                .username(user.getUsername())
                .password("password")
                .build();

        webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDTO.class)
                .value(response -> {
                    assert response.getUserId() != null;
                    assert response.getToken() != null;
                });
    }
}

