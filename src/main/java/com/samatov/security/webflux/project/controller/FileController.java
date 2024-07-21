package com.samatov.security.webflux.project.controller;

import com.samatov.security.webflux.project.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/file")
@Validated
@Slf4j
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public Mono<ResponseEntity<String>> uploadFile(@RequestPart(value = "file") Mono<FilePart> filePart) {
        return filePart
                .flatMap(fileService::uploadFile)
                .map(fileResponse -> ResponseEntity.ok().body("Upload successfully: " + fileResponse))
                .doOnError(error -> log.error("Error during file upload: " + error.getMessage()))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
}
