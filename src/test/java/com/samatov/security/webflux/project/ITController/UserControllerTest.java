//package com.samatov.security.webflux.project.ITController;
//
//import com.samatov.security.webflux.project.config.PostgresTestContainerConfig;
//import com.samatov.security.webflux.project.dto.UserDTO;
//import com.samatov.security.webflux.project.errorhandling.AppErrorAttributes;
//import com.samatov.security.webflux.project.errorhandling.AppErrorWebExceptionHandler;
//import com.samatov.security.webflux.project.mapper.UserMapper;
//import com.samatov.security.webflux.project.model.User;
//import com.samatov.security.webflux.project.repository.UserRepository;
//import com.samatov.security.webflux.project.security.SecurityService;
//import com.samatov.security.webflux.project.service.UserService;
//import com.samatov.security.webflux.project.utils.TestUtils;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.reactive.server.WebTestClient;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@AutoConfigureWebTestClient
//@Import({TestSecurityConfig.class,
//        JwtTokenUtil.class,
//        UserMapper.class,
//        SecurityService.class,
//        UserService.class,
//        PostgresTestContainerConfig.class,
//        TestConfig.class,
//        AppErrorWebExceptionHandler.class,
//        AppErrorAttributes.class
//})
//@ActiveProfiles("test")
//public class UserControllerTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private UserMapper userMapper;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;
//
//    private User user;
//    private UserDTO userDTO;
//
//    @BeforeEach
//    public void setUp() {
//        user = TestUtils.createUser();
//        userDTO = TestUtils.createUserDTO();
//
//        userRepository.save(user).block();
//    }
//
//    @Test
//    @DisplayName("Получение пользователя по ID")
//    public void testGetUserById() {
//        String token = jwtTokenUtil.createTokenForUser();
//
//        webTestClient.get().uri("/api/v1/user/{id}", user.getId())
//                .header("Authorization", "Bearer " + token)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserDTO.class)
//                .consumeWith(response -> {
//                    UserDTO responseBody = response.getResponseBody();
//                    assertThat(responseBody).isEqualTo(userDTO);
//                });
//    }
//
//    @Test
//    @DisplayName("Получение всех пользователей")
//    public void testGetAllUsers() {
//        String token = jwtTokenUtil.createTokenForModerator();
//
//        webTestClient.get().uri("/api/v1/user/all")
//                .header("Authorization", "Bearer " + token)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(UserDTO.class)
//                .consumeWith(response -> {
//                    List<UserDTO> responseBody = response.getResponseBody();
//                    assertThat(responseBody).hasSize(1);
//                    assertThat(responseBody.get(0)).isEqualTo(userDTO);
//                });
//    }
//
//    @Test
//    @DisplayName("Создание нового пользователя")
//    public void testCreateUser() {
//        String token = jwtTokenUtil.createTokenForAdmin();
//
//        webTestClient.post().uri("/api/v1/user/save")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + token)
//                .bodyValue(userDTO)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserDTO.class)
//                .consumeWith(response -> {
//                    UserDTO responseBody = response.getResponseBody();
//                    assertThat(responseBody).isEqualTo(userDTO);
//                });
//    }
//
//    @Test
//    @DisplayName("Обновление пользователя")
//    public void testUpdateUser() {
//        User updatedUser = TestUtils.createUpdatedUser(user);
//        UserDTO updatedUserDTO = TestUtils.createUpdatedUserDTO(userDTO);
//
//        String token = jwtTokenUtil.createTokenForAdmin();
//
//        webTestClient.put().uri("/api/v1/user/update")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + token)
//                .bodyValue(updatedUserDTO)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserDTO.class)
//                .consumeWith(response -> {
//                    UserDTO responseBody = response.getResponseBody();
//                    assertThat(responseBody).isEqualTo(updatedUserDTO);
//                });
//    }
//
//    @Test
//    @DisplayName("Удаление пользователя по ID - пользователь существует")
//    public void testDeleteUserById_UserExists() {
//        String token = jwtTokenUtil.createTokenForAdmin();
//
//        webTestClient.delete().uri("/api/v1/user/{id}", user.getId())
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                .exchange()
//                .expectStatus().isNoContent();
//    }
//
//    @Test
//    @DisplayName("Удаление пользователя по ID - пользователь не найден")
//    public void testDeleteUserById_UserNotFound() {
//        String token = jwtTokenUtil.createTokenForAdmin();
//
//        webTestClient.delete().uri("/api/v1/user/{id}", 999L)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//}
