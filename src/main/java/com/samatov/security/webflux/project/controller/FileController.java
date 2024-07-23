package com.samatov.security.webflux.project.controller;

import com.samatov.security.webflux.project.dto.FileDTO;
import com.samatov.security.webflux.project.mapper.FileMapper;
import com.samatov.security.webflux.project.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/file")
@Validated
@Slf4j
public class FileController {
    private final FileService fileService;
    private final FileMapper fileMapper;


    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @PostMapping("/upload")
    public Mono<ResponseEntity<FileDTO>> uploadFile(@RequestPart(value = "file") Mono<FilePart> filePart) {
        return filePart
                .flatMap(fileService::uploadFile)
                .map(fileEntity -> {
                    FileDTO fileDTO = fileMapper.map(fileEntity);
                    return ResponseEntity.ok().body(fileDTO);
                })
                .doOnError(error -> log.error("Error during file upload: " + error.getMessage()))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<FileDTO>> getFileById(@PathVariable Long id) {
        return fileService.getFileById(id)
                .map(fileEntity -> ResponseEntity.ok(fileMapper.map(fileEntity)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/all")
    public Flux<FileDTO> getAllFiles() {
        return fileService.getAllFiles()
                .map(fileMapper::map);
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteFile(@PathVariable Long id) {
        return fileService.deleteFile(id)
                .map(v -> ResponseEntity.noContent().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/download/{filename}")
    public Mono<ResponseEntity<Flux<DataBuffer>>> getFileByName(@PathVariable String filename) {
        return fileService.getFileByName(filename)
                .map(buffer -> {
                    DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(buffer);
                    Flux<DataBuffer> dataBufferFlux = Flux.just(dataBuffer);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(dataBufferFlux);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
