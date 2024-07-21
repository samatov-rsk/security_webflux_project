package com.samatov.security.webflux.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final S3AsyncClient s3Client;

    @Value("${application.bucket.name}")
    private String bucketName;

    public Mono<String> uploadFile(FilePart filePart) {
        String filename = System.currentTimeMillis() + "_" + filePart.filename();
        AtomicReference<ByteBuffer> byteBufferRef = new AtomicReference<>();

        return filePart.content()
                .map(dataBuffer -> {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
                    dataBuffer.read(byteBuffer.array());
                    byteBuffer.flip();
                    byteBufferRef.set(byteBuffer);
                    return byteBufferRef.get();
                })
                .last()
                .flatMap(buffer -> {
                    CompletableFuture<PutObjectResponse> future = s3Client.putObject(
                            PutObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(filename)
                                    .build(),
                            AsyncRequestBody.fromByteBuffer(buffer)
                    );

                    return Mono.fromFuture(future)
                            .map(response -> "File uploaded: " + filename);
                });
    }
}
