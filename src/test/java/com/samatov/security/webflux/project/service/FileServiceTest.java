package com.samatov.security.webflux.project.service;

import com.samatov.security.webflux.project.enums.Status;
import com.samatov.security.webflux.project.exception.NotFoundException;
import com.samatov.security.webflux.project.model.FileEntity;
import com.samatov.security.webflux.project.repository.FileRepository;
import com.samatov.security.webflux.project.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private EventService eventService;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Когда uploadFile вызывается, то успешный результат")
    void uploadFileTest() {
        // Mock FilePart
        FilePart filePart = mock(FilePart.class);
        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap("content".getBytes());
        when(filePart.filename()).thenReturn("test-file.txt");
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));

        // Mock S3Service
        when(s3Service.uploadFileToS3(any(), any())).thenReturn(Mono.just(PutObjectResponse.builder().build()));

        // Mock FileRepository save method
        FileEntity fileEntity = FileEntity.builder()
                .id(1L)
                .name("test-file.txt")
                .location("s3://bucket/test-file.txt")
                .status(Status.ACTIVE)
                .build();
        when(fileRepository.save(any())).thenReturn(Mono.just(fileEntity));

        Mono<FileEntity> result = fileService.uploadFile(filePart);

    }

    @Test
    @DisplayName("Когда getFileById вызывается, то успешный результат")
    void getFileByIdTest() {
        FileEntity fileEntity = new FileEntity();
        when(fileRepository.findById(anyLong())).thenReturn(Mono.just(fileEntity));

        Mono<FileEntity> result = fileService.getFileById(1L);

        assertEquals(fileEntity, result.block());
        verify(fileRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Когда getFileById вызывается, то NotFoundException")
    void getFileByIdNotFoundTest() {
        when(fileRepository.findById(anyLong())).thenReturn(Mono.empty());

        assertThrows(NotFoundException.class, () -> fileService.getFileById(1L).block());
        verify(fileRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Когда getAllFiles вызывается, то успешный результат")
    void getAllFilesTest() {
        FileEntity fileEntity1 = new FileEntity();
        fileEntity1.setId(1L);
        FileEntity fileEntity2 = new FileEntity();
        fileEntity2.setId(2L);

        when(fileRepository.findAll()).thenReturn(Flux.just(fileEntity1, fileEntity2));

        Flux<FileEntity> result = fileService.getAllFiles();

        StepVerifier.create(result)
                .expectNext(fileEntity1)
                .expectNext(fileEntity2)
                .verifyComplete();

        verify(fileRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Когда deleteFile вызывается, то NotFoundException")
    void deleteFileNotFoundTest() {
        when(fileRepository.findById(anyLong())).thenReturn(Mono.empty());

        assertThrows(NotFoundException.class, () -> fileService.deleteFile(1L).block());
        verify(fileRepository, times(1)).findById(anyLong());
    }


}
