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
public class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private String adminToken;

    @BeforeEach
    public void setUp() {
        eventRepository.deleteAll()
                .then(userRepository.deleteAll())
                .block();

        adminUser = createAdminUser("admin");
        adminUser.setRoles(Role.ADMIN);
        adminToken = authenticateAndGetToken(adminUser.getUsername(), "password");

        createUser("user1");
        createUser("user2");
        createUser("user3");
    }

    private User createUser(String username) {
        User user = TestUtils.createUser();
        user.setUsername(username);
        return userService.registerUser(user).block();
    }

    private User createAdminUser(String username) {
        User user = TestUtils.createAdminUser();
        user.setUsername(username);
        user.setRoles(Role.ADMIN); // Обязательно задаем роль ADMIN
        return userService.registerUser(user).block();
    }

    private String authenticateAndGetToken(String username, String password) {
        AuthRequestDTO loginRequest = AuthRequestDTO.builder()
                .username(username)
                .password(password)
                .build();

        AuthResponseDTO response = webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assert response != null;
        return response.getToken();
    }

    @Test
    @DisplayName("Получение всех пользователей")
    public void testGetAllUsers() {
        webTestClient.get().uri("/api/v1/user/all")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .value(users -> {
                    assert users.size() >= 3;
                    users.forEach(user -> {
                        System.out.println("User: " + user);
                        assert user.getId() != null;
                        assert user.getUsername() != null;
                    });
                });
    }

    @Test
    @DisplayName("Получение пользователя по ID")
    public void testGetUserById() {
        User savedUser = createAdminUser("userToGet");

        webTestClient.get().uri("/api/v1/user/{id}", savedUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .value(response -> {
                    assert response.getId().equals(savedUser.getId());
                    assert response.getUsername().equals(savedUser.getUsername());
                    assert response.getRoles().equals(savedUser.getRoles());
                    assert response.getStatus().equals(savedUser.getStatus());
                });
    }

    @Test
    @DisplayName("Создание нового пользователя")
    public void testCreateUser() {
        UserDTO userDTO = TestUtils.createUserDTO();
        userDTO.setUsername("newUser");
        userDTO.setPassword("newPassword");

        webTestClient.post().uri("/api/v1/user/save")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .value(response -> {
                    assert response.getUsername().equals(userDTO.getUsername());
                    assert response.getRoles().equals(Role.USER);
                    assert response.getStatus().equals(Status.ACTIVE);
                });
    }

    @Test
    @DisplayName("Обновление существующего пользователя")
    public void testUpdateUser() {
        User savedUser = createAdminUser("userToUpdate");
        UserDTO updatedUserDTO = userMapper.map(savedUser);
        updatedUserDTO.setUsername("updatedUser");

        webTestClient.put().uri("/api/v1/user/update")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedUserDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .value(response -> {
                    assert response.getId().equals(savedUser.getId());
                    assert response.getUsername().equals("updatedUser");
                });
    }

    @Test
    @DisplayName("Удаление пользователя по ID")
    public void testDeleteUserById() {
        User userToDelete = createAdminUser("userToDelete");

        webTestClient.delete().uri("/api/v1/user/{id}", userToDelete.getId())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя по ID")
    public void testDeleteNonExistingUserById() {
        // Попытка удалить несуществующего пользователя с ID 9999
        webTestClient.delete().uri("/api/v1/user/{id}", 9999L)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectBody()
                .jsonPath("$.errors[0].message").isEqualTo("Пользователь с таким айди не найден");
    }
}
