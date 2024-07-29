package com.samatov.security.webflux.project.ITController;

import com.samatov.security.webflux.project.config.PostgresTestContainerConfig;
import com.samatov.security.webflux.project.config.TestConfig;
import com.samatov.security.webflux.project.config.TestSecurityConfig;
import com.samatov.security.webflux.project.controller.FileController;
import com.samatov.security.webflux.project.dto.FileDTO;
import com.samatov.security.webflux.project.errorhandling.AppErrorAttributes;
import com.samatov.security.webflux.project.errorhandling.AppErrorWebExceptionHandler;
import com.samatov.security.webflux.project.exception.NotFoundException;
import com.samatov.security.webflux.project.mapper.FileMapper;
import com.samatov.security.webflux.project.model.FileEntity;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.repository.EventRepository;
import com.samatov.security.webflux.project.repository.FileRepository;
import com.samatov.security.webflux.project.repository.UserRepository;
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
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = FileController.class)
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
public class FileControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private FileService fileService;

    @MockBean
    private FileMapper fileMapper;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FileRepository fileRepository;


    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private FileEntity fileEntity;
    private FileDTO fileDTO;
    private String token;

    @BeforeEach
    public void setUp() {
        fileEntity = TestUtils.createFileEntity();
        fileDTO = TestUtils.createFileDTO();
        token = jwtTokenUtil.createTokenForAdmin();

        User mockUser = TestUtils.createUser();
        when(userRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Mono.just(mockUser));
        when(fileService.getFileById(any(Long.class))).thenReturn(Mono.just(fileEntity));
    }

    @Test
    @DisplayName("Загрузка файла")
    public void testUploadFile() {
        when(fileService.uploadFile(ArgumentMatchers.any())).thenReturn(Mono.just(fileEntity));
        when(fileMapper.map(ArgumentMatchers.any(FileEntity.class))).thenReturn(fileDTO);

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", "Test Content".getBytes())
                .header("Content-Disposition", "form-data; name=file; filename=test.txt");

        webTestClient.post().uri("/api/v1/file/upload")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(FileDTO.class)
                .isEqualTo(fileDTO);
    }

    @Test
    @DisplayName("Получение файла по ID")
    public void testGetFileById() {
        when(fileService.getFileById(ArgumentMatchers.anyLong())).thenReturn(Mono.just(fileEntity));
        when(fileMapper.map(ArgumentMatchers.any(FileEntity.class))).thenReturn(fileDTO);

        webTestClient.get().uri("/api/v1/file/{id}", fileEntity.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FileDTO.class)
                .isEqualTo(fileDTO);
    }

    @Test
    @DisplayName("Получение файла по ID - не найдено")
    public void testGetFileById_NotFound() {
        when(fileService.getFileById(ArgumentMatchers.anyLong())).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/file/{id}", fileEntity.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Получение всех файлов")
    public void testGetAllFiles() {
        List<FileEntity> fileEntities = Arrays.asList(fileEntity, TestUtils.createFileEntity());
        List<FileDTO> fileDTOs = Arrays.asList(fileDTO, TestUtils.createFileDTO());

        when(fileService.getAllFiles()).thenReturn(Flux.fromIterable(fileEntities));
        when(fileMapper.map(ArgumentMatchers.any(FileEntity.class)))
                .thenReturn(fileDTO, fileDTOs.get(1));

        webTestClient.get().uri("/api/v1/file/all")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FileDTO.class)
                .isEqualTo(fileDTOs);
    }


    @Test
    @DisplayName("Удаление файла")
    public void testDeleteFile() {
        when(fileService.deleteFile(fileEntity.getId())).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/file/{id}", fileEntity.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Удаление файла - не найдено")
    public void testDeleteFile_NotFound() {
        when(fileService.deleteFile(ArgumentMatchers.anyLong())).thenReturn(Mono.error(
                new NotFoundException("Файл с таким id не найден")));

        webTestClient.delete().uri("/api/v1/file/{id}", fileEntity.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Скачивание файла")
    public void testDownloadFile() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/test/resources/test-files/testfile.txt")));
        when(fileService.getFileByName("testfile.txt")).thenReturn(Mono.just(byteBuffer));

        webTestClient.get().uri("/api/v1/file/download/testfile.txt")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"testfile.txt\"")
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectBody(byte[].class).isEqualTo(Files.readAllBytes(Paths.get("src/test/resources/test-files/testfile.txt")));
    }

    @Test
    @DisplayName("Скачивание файла - не найдено")
    public void testDownloadFile_NotFound() {
        when(fileService.getFileByName("nonexistentfile.txt")).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/file/download/nonexistentfile.txt")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }
}
