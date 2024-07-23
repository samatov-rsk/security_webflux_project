package com.samatov.security.webflux.project.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@Service
public class S3Service {

    private final S3AsyncClient s3Client;
    private final String bucketName;

    public S3Service(S3AsyncClient s3Client, @Value("${application.bucket.name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public Mono<PutObjectResponse> uploadFileToS3(String filename, ByteBuffer buffer) {
        CompletableFuture<PutObjectResponse> future = s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filename)
                        .build(),
                AsyncRequestBody.fromByteBuffer(buffer)
        );

        return Mono.fromFuture(future);
    }

    public String getBucketName() {
        return bucketName;
    }
}