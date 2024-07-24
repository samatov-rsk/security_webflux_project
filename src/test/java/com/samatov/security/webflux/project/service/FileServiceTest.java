package com.samatov.security.webflux.project.service;

import com.samatov.security.webflux.project.enums.Status;
import com.samatov.security.webflux.project.exception.NotFoundException;
import com.samatov.security.webflux.project.model.FileEntity;
import com.samatov.security.webflux.project.repository.FileRepository;
import com.samatov.security.webflux.project.s3.S3Service;
import com.samatov.security.webflux.project.security.CustomPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class FileServiceTest {

    @Mock
    private S3AsyncClient s3Client;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private EventService eventService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private FileService fileService;

    private FileEntity fileEntity;
    private FilePart filePart;
    private DataBuffer dataBuffer;
    private CustomPrincipal customPrincipal;
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        fileEntity = new FileEntity();
        fileEntity.setId(1L);
        fileEntity.setName("test.txt");
        fileEntity.setLocation("s3://bucket/test.txt");
        fileEntity.setStatus(Status.ACTIVE);

        filePart = mock(FilePart.class);
        when(filePart.filename()).thenReturn("test.txt");

        dataBuffer = mock(DataBuffer.class);
        when(dataBuffer.readableByteCount()).thenReturn(10);
        when(dataBuffer.read(any())).thenAnswer(invocation -> {
            ByteBuffer buffer = (ByteBuffer) invocation.getArgument(0);
            buffer.put("content".getBytes());
            return null;
        });
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));

        customPrincipal = new CustomPrincipal(1L, "username");
        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(customPrincipal);
        ReactiveSecurityContextHolder.withAuthentication(authentication);

        when(s3Service.uploadFileToS3(anyString(), any(ByteBuffer.class)))
                .thenReturn(Mono.just(PutObjectResponse.builder().build()));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(Mono.just(fileEntity));
        when(eventService.createEvent(anyLong(), anyLong())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Когда uploadFile вызывается, то успешный результат")
    void uploadFileTest() {
        Mono<FileEntity> result = fileService.uploadFile(filePart);

        StepVerifier.create(result)
                .expectNextMatches(file -> file.getName().equals("test.txt") &&
                        file.getLocation().equals("s3://bucket/test.txt") &&
                        file.getStatus() == Status.ACTIVE)
                .verifyComplete();
    }

    @Test
    @DisplayName("Когда getFileById вызывается, то успешный результат")
    void getFileByIdTest() {
        when(fileRepository.findById(1L)).thenReturn(Mono.just(fileEntity));

        Mono<FileEntity> result = fileService.getFileById(1L);

        StepVerifier.create(result)
                .expectNextMatches(file -> file.getId().equals(1L) &&
                        file.getName().equals("test.txt"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Когда getFileById вызывается с несуществующим id, то NotFoundException")
    void getFileByIdNotFoundTest() {
        when(fileRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<FileEntity> result = fileService.getFileById(1L);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Когда deleteFile вызывается, то успешный результат")
    void deleteFileTest() {
        when(fileRepository.findById(1L)).thenReturn(Mono.just(fileEntity));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(Mono.just(fileEntity));

        DeleteObjectResponse deleteObjectResponse = DeleteObjectResponse.builder().build();
        CompletableFuture<DeleteObjectResponse> future = CompletableFuture.completedFuture(deleteObjectResponse);
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(future);

        Mono<Void> result = fileService.deleteFile(1L);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Когда getFileByName вызывается, то успешный результат")
    void getFileByNameTest() {
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), "content".getBytes());
        CompletableFuture<ResponseBytes<GetObjectResponse>> future = CompletableFuture.completedFuture(responseBytes);
        when(s3Client.getObject(any(GetObjectRequest.class), any(AsyncResponseTransformer.class)))
                .thenReturn(future);

        Mono<ByteBuffer> result = fileService.getFileByName("test.txt");

        StepVerifier.create(result)
                .expectNextMatches(buffer -> {
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    return new String(bytes).equals("content");
                })
                .verifyComplete();
    }
}
