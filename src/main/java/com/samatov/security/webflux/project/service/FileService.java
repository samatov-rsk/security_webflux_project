package com.samatov.security.webflux.project.service;

import com.samatov.security.webflux.project.enums.Status;
import com.samatov.security.webflux.project.model.FileEntity;
import com.samatov.security.webflux.project.repository.FileRepository;
import com.samatov.security.webflux.project.s3.S3Service;
import com.samatov.security.webflux.project.security.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final S3AsyncClient s3Client;
    private final FileRepository fileRepository;
    private final EventService eventService;
    private final S3Service s3Service;

    @Value("${application.bucket.name}")
    private String bucketName;

    public Mono<FileEntity> uploadFile(FilePart filePart) {
        String filename = filePart.filename();
        return getCurrentUserId()
                .flatMap(userId -> processFileContent(filePart)
                        .flatMap(buffer -> s3Service.uploadFileToS3(filename, buffer))
                        .flatMap(response -> saveFileEntity(filename))
                        .flatMap(savedFile -> eventService.createEvent(userId, savedFile.getId())
                                .thenReturn(savedFile))
                );
    }

    private Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    if (authentication != null && authentication.getPrincipal() instanceof CustomPrincipal) {
                        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
                        return Mono.just(customPrincipal.getId());
                    } else {
                        return Mono.error(new RuntimeException("User is not authenticated"));
                    }
                });
    }

    private Mono<ByteBuffer> processFileContent(FilePart filePart) {
        AtomicReference<ByteBuffer> byteBufferRef = new AtomicReference<>();

        return filePart.content()
                .map(dataBuffer -> {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
                    dataBuffer.read(byteBuffer.array());
                    byteBuffer.flip();
                    byteBufferRef.set(byteBuffer);
                    return byteBufferRef.get();
                })
                .last();
    }

    private Mono<FileEntity> saveFileEntity(String filename) {
        FileEntity fileEntity = FileEntity.builder()
                .name(filename)
                .location("s3://" + s3Service.getBucketName() + "/" + filename)
                .status(Status.ACTIVE)
                .build();

        return fileRepository.save(fileEntity);
    }

    public Mono<FileEntity> getFileById(Long id) {
        return fileRepository.findById(id);
    }

    public Flux<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    public Mono<Void> deleteFile(Long id) {
        return fileRepository.findById(id)
                .flatMap(fileEntity -> {
                    CompletableFuture<DeleteObjectResponse> future = s3Client.deleteObject(
                            DeleteObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(fileEntity.getName())
                                    .build()
                    );

                    return Mono.fromFuture(future)
                            .flatMap(response -> {
                                fileEntity.setStatus(Status.DELETED);
                                return fileRepository.save(fileEntity).then();
                            });
                });
    }

    public Mono<ByteBuffer> getFileByName(String filename) {
        log.info("Fetching file: {} from bucket: {}", filename, bucketName);
        CompletableFuture<ResponseBytes<GetObjectResponse>> future = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filename)
                        .build(),
                AsyncResponseTransformer.toBytes()
        );

        return Mono.fromFuture(future)
                .map(responseBytes -> ByteBuffer.wrap(responseBytes.asByteArray()))
                .doOnError(error -> log.error("Error fetching file from S3", error));
    }
}
