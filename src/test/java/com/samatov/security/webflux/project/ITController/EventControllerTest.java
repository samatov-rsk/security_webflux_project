//package com.samatov.security.webflux.project.ITController;
//
//import com.samatov.security.webflux.project.config.PostgresTestContainerConfig;
//import com.samatov.security.webflux.project.dto.EventDTO;
//import com.samatov.security.webflux.project.dto.FileDTO;
//import com.samatov.security.webflux.project.dto.UserDTO;
//import com.samatov.security.webflux.project.errorhandling.AppErrorAttributes;
//import com.samatov.security.webflux.project.errorhandling.AppErrorWebExceptionHandler;
//import com.samatov.security.webflux.project.mapper.EventMapper;
//import com.samatov.security.webflux.project.mapper.FileMapper;
//import com.samatov.security.webflux.project.mapper.UserMapper;
//import com.samatov.security.webflux.project.model.Event;
//import com.samatov.security.webflux.project.model.FileEntity;
//import com.samatov.security.webflux.project.model.User;
//import com.samatov.security.webflux.project.repository.EventRepository;
//import com.samatov.security.webflux.project.repository.FileRepository;
//import com.samatov.security.webflux.project.repository.UserRepository;
//import com.samatov.security.webflux.project.security.SecurityService;
//import com.samatov.security.webflux.project.service.EventService;
//import com.samatov.security.webflux.project.service.FileService;
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
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.reactive.server.WebTestClient;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@SpringBootTest
//@AutoConfigureWebTestClient
//@Import({
//        WebTestClient.class,
//        TestSecurityConfig.class,
//        JwtTokenUtil.class,
//        SecurityService.class,
//        EventService.class,
//        FileService.class,
//        UserService.class,
//        EventMapper.class,
//        UserMapper.class,
//        FileMapper.class,
//        PostgresTestContainerConfig.class,
//        TestConfig.class,
//        AppErrorWebExceptionHandler.class,
//        AppErrorAttributes.class
//})
//@ActiveProfiles("test")
//public class EventControllerTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @Autowired
//    private EventService eventService;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private FileService fileService;
//
//    @Autowired
//    private EventMapper eventMapper;
//
//    @Autowired
//    private UserMapper userMapper;
//
//    @Autowired
//    private FileMapper fileMapper;
//
//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;
//
//    @Autowired
//    private EventRepository eventRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private FileRepository fileRepository;
//
//    private Event event;
//    private User user;
//    private FileEntity fileEntity;
//    private EventDTO eventDTO;
//    private UserDTO userDTO;
//    private FileDTO fileDTO;
//    private String token;
//
//    @BeforeEach
//    public void setUp() {
//        event = TestUtils.createEvent();
//        user = TestUtils.createUser();
//        fileEntity = TestUtils.createFileEntity();
//        userDTO = TestUtils.createUserDTO();
//        fileDTO = TestUtils.createFileDTO();
//        eventDTO = TestUtils.createEventDTO();
//
//        token = jwtTokenUtil.createTokenForAdmin();
//
//        userRepository.save(user).block();
//        fileRepository.save(fileEntity).block();
//        eventRepository.save(event).block();
//    }
//
//    @Test
//    @DisplayName("Получение события по ID - успех")
//    public void testGetEventById_Success() {
//        webTestClient.get().uri("/api/v1/events/{id}", event.getId())
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(EventDTO.class)
//                .isEqualTo(eventDTO);
//    }
//
//    @Test
//    @DisplayName("Получение события по ID - не найдено")
//    public void testGetEventById_NotFound() {
//        webTestClient.get().uri("/api/v1/events/{id}", 999L)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    @DisplayName("Получение всех событий - успех")
//    public void testGetAllEvents_Success() {
//        webTestClient.get().uri("/api/v1/events/all")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(EventDTO.class)
//                .consumeWith(response -> {
//                    List<EventDTO> responseBody = response.getResponseBody();
//                    assertNotNull(responseBody);
//                    assert responseBody.size() == 1;
//                    assert responseBody.get(0).equals(eventDTO);
//                });
//    }
//}
