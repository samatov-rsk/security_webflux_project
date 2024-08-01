package com.samatov.security.webflux.project.ITController;

import com.samatov.security.webflux.project.config.PostgresTestContainerConfig;
import com.samatov.security.webflux.project.dto.*;
import com.samatov.security.webflux.project.errorhandling.AppErrorAttributes;
import com.samatov.security.webflux.project.errorhandling.AppErrorWebExceptionHandler;
import com.samatov.security.webflux.project.mapper.EventMapper;
import com.samatov.security.webflux.project.mapper.FileMapper;
import com.samatov.security.webflux.project.mapper.UserMapper;
import com.samatov.security.webflux.project.model.Event;
import com.samatov.security.webflux.project.model.FileEntity;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.repository.EventRepository;
import com.samatov.security.webflux.project.repository.FileRepository;
import com.samatov.security.webflux.project.repository.UserRepository;
import com.samatov.security.webflux.project.service.EventService;
import com.samatov.security.webflux.project.service.FileService;
import com.samatov.security.webflux.project.service.UserService;
import com.samatov.security.webflux.project.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import({
        PostgresTestContainerConfig.class,
        EventService.class,
        FileService.class,
        UserService.class,
        AppErrorWebExceptionHandler.class,
        AppErrorAttributes.class
})
@ActiveProfiles("test")
@Testcontainers
public class EventControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Event event;
    private User user;
    private FileEntity fileEntity;
    private EventDTO eventDTO;
    private UserDTO userDTO;
    private FileDTO fileDTO;
    private String token;

    @BeforeEach
    public void setUp() {
        cleanDatabase();
        initializeData();
        authenticateUser();
    }

    private void cleanDatabase() {
        eventRepository.deleteAll().block();
        userRepository.deleteAll().block();
        fileRepository.deleteAll().block();
    }

    private void initializeData() {
        user = userService.registerUser(TestUtils.createUser()).block();
        assertNotNull(user);
        System.out.println("Registered user ID: " + user.getId());

        fileEntity = fileRepository.save(TestUtils.createFileEntity()).block();
        assertNotNull(fileEntity);
        System.out.println("Saved fileEntity ID: " + fileEntity.getId());

        event = TestUtils.createEvent(user.getId(), fileEntity.getId());
        event = eventRepository.save(event).block();
        assertNotNull(event);
        System.out.println("Saved event ID: " + event.getId());

        userDTO = TestUtils.createUserDTOEvents(user);
        fileDTO = TestUtils.createFileDTOEvents(fileEntity);
        eventDTO = TestUtils.createEventDTO(userDTO, fileDTO);

        assertNotNull(user.getId());
        assertNotNull(fileEntity.getId());
        assertNotNull(event.getId());
    }

    private void authenticateUser() {
        token = authenticateAndGetToken(user.getUsername(), "password");
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

        assertNotNull(response);
        return response.getToken();
    }

    @Test
    @DisplayName("Получение события по ID - успех")
    public void testGetEventById_Success() {
        webTestClient.get().uri("/api/v1/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventDTO.class)
                .consumeWith(response -> {
                    EventDTO actualEventDTO = response.getResponseBody();
                    assertNotNull(actualEventDTO);
                    assertEventDTOEquals(eventDTO, actualEventDTO);
                });
    }

    @Test
    @DisplayName("Получение события по ID - не найдено")
    public void testGetEventById_NotFound() {
        webTestClient.get().uri("/api/v1/events/{id}", 999L)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectBody()
                .jsonPath("$.errors[0].message").isEqualTo("Событие с таким id не найдено");
    }


    @Test
    @DisplayName("Получение всех событий - успех")
    public void testGetAllEvents_Success() {
        webTestClient.get().uri("/api/v1/events/all")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EventDTO.class)
                .consumeWith(response -> {
                    List<EventDTO> responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals(1, responseBody.size());
                    assertEventDTOEquals(eventDTO, responseBody.get(0));
                });
    }

    private void assertEventDTOEquals(EventDTO expected, EventDTO actual) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertUserDTOEquals(expected.getUser(), actual.getUser());
        assertFileDTOEquals(expected.getFile(), actual.getFile());
    }

    private void assertUserDTOEquals(UserDTO expected, UserDTO actual) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getRoles(), actual.getRoles());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    private void assertFileDTOEquals(FileDTO expected, FileDTO actual) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getLocation(), actual.getLocation());
        assertEquals(expected.getStatus(), actual.getStatus());
    }
}
