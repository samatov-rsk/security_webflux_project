package com.samatov.security.webflux.project.ITController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samatov.security.webflux.project.config.PostgresTestContainerConfig;
import com.samatov.security.webflux.project.config.TestSecurityConfig;
import com.samatov.security.webflux.project.dto.AuthRequestDTO;
import com.samatov.security.webflux.project.dto.AuthResponseDTO;
import com.samatov.security.webflux.project.dto.FileDTO;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.service.FileService;
import com.samatov.security.webflux.project.service.UserService;
import com.samatov.security.webflux.project.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainerConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Testcontainers
public class FileControllerTest {

    @Container
    public static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.S3)
            .withEnv("DEFAULT_REGION", "us-east-1")
            .waitingFor(Wait.forLogMessage(".*Ready.*", 1));

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserService userService;

    @InjectMocks
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private String userToken;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        user = TestUtils.createUser();
        userService.registerUser(user).block();

        userToken = authenticateAndGetToken(user.getUsername(), user.getPassword());

        S3Client s3Client = createLocalstackS3Client();
        createBucket(s3Client, "my-bucket");
    }

    private S3Client createLocalstackS3Client() {
        return S3Client.builder()
                .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
                .build();
    }

    private void createBucket(S3Client s3Client, String bucketName) {
        try {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.createBucket(createBucketRequest);
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }

    private String authenticateAndGetToken(String username, String password) {
        AuthRequestDTO loginRequest = new AuthRequestDTO(username, password);
        AuthResponseDTO response = webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDTO.class)
                .returnResult()
                .getResponseBody();
        return response != null ? response.getToken() : null;
    }

    @Test
    @DisplayName("Загрузка нового файла")
    public void testUploadFile() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-file.txt"))
                .header("Content-Disposition", "form-data; name=file; filename=test-file.txt");

        webTestClient.post().uri("/api/v1/file/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + userToken)
                .bodyValue(builder.build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(FileDTO.class)
                .value(response -> {
                    assertEquals("test-file.txt", response.getName());
                    assertEquals("ACTIVE", response.getStatus());
                });
    }
}
