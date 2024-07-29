package com.samatov.security.webflux.project.ITController;

import com.samatov.security.webflux.project.config.PostgresTestContainerConfig;
import com.samatov.security.webflux.project.config.TestConfig;
import com.samatov.security.webflux.project.config.TestSecurityConfig;
import com.samatov.security.webflux.project.controller.EventController;
import com.samatov.security.webflux.project.dto.EventDTO;
import com.samatov.security.webflux.project.dto.FileDTO;
import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.errorhandling.AppErrorAttributes;
import com.samatov.security.webflux.project.errorhandling.AppErrorWebExceptionHandler;
import com.samatov.security.webflux.project.mapper.EventMapper;
import com.samatov.security.webflux.project.mapper.FileMapper;
import com.samatov.security.webflux.project.mapper.UserMapper;
import com.samatov.security.webflux.project.model.Event;
import com.samatov.security.webflux.project.model.FileEntity;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.security.SecurityService;
import com.samatov.security.webflux.project.service.EventService;
import com.samatov.security.webflux.project.service.FileService;
import com.samatov.security.webflux.project.service.UserService;
import com.samatov.security.webflux.project.utils.JwtTokenUtil;
import com.samatov.security.webflux.project.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = EventController.class)
@Import({
        TestSecurityConfig.class,
        JwtTokenUtil.class,
        SecurityService.class,
        UserService.class,
        EventService.class,
        FileService.class,
        PostgresTestContainerConfig.class,
        TestConfig.class,
        AppErrorWebExceptionHandler.class,
        AppErrorAttributes.class
})
@ActiveProfiles("test")
public class EventControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private EventService eventService;

    @MockBean
    private UserService userService;

    @MockBean
    private FileService fileService;

    @MockBean
    private EventMapper eventMapper;

    @MockBean
    private FileMapper fileMapper;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private Event event;
    private User user;
    private FileEntity fileEntity;
    private EventDTO eventDTO;
    private UserDTO userDTO;
    private FileDTO fileDTO;
    private String token;

    @BeforeEach
    public void setUp() {
        event = TestUtils.createEvent();
        user = TestUtils.createUser();
        fileEntity = TestUtils.createFileEntity();
        userDTO = TestUtils.createUserDTO();
        fileDTO = TestUtils.createFileDTO();
        eventDTO = TestUtils.createEventDTO();

        token = jwtTokenUtil.createTokenForAdmin();

        when(eventService.getEventById(ArgumentMatchers.anyLong())).thenReturn(Mono.just(event));
        when(userService.getUserById(ArgumentMatchers.anyLong())).thenReturn(Mono.just(user));
        when(fileService.getFileById(ArgumentMatchers.anyLong())).thenReturn(Mono.just(fileEntity));
        when(userMapper.map(ArgumentMatchers.any(User.class))).thenReturn(userDTO);
        when(fileMapper.map(ArgumentMatchers.any(FileEntity.class))).thenReturn(fileDTO);
        when(eventMapper.map(ArgumentMatchers.any(Event.class))).thenReturn(eventDTO);
    }

    @Test
    @DisplayName("Успешное получение события по ID")
    public void testGetEventById_Success() {

        webTestClient.get().uri("/api/v1/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventDTO.class)
                .isEqualTo(eventDTO);
    }

    @Test
    @DisplayName("Получение события по ID - не найдено")
    public void testGetEventById_NotFound() {
        when(eventService.getEventById(event.getId())).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Успешное получение всех событий")
    public void testGetAllEvents_Success() {
        when(eventService.getAllEvents()).thenReturn(Flux.just(event));
        when(userService.getUserById(event.getUserId())).thenReturn(Mono.just(user));
        when(fileService.getFileById(event.getFileId())).thenReturn(Mono.just(fileEntity));
        when(userMapper.map(any(User.class))).thenReturn(userDTO);
        when(fileMapper.map(any(FileEntity.class))).thenReturn(fileDTO);
        when(eventMapper.map(any(Event.class))).thenReturn(eventDTO);

        webTestClient.get().uri("/api/v1/events/all")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EventDTO.class)
                .consumeWith(response -> {
                    List<EventDTO> responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assert responseBody.size() == 1;
                    assert responseBody.get(0).equals(eventDTO);
                });
    }
}
