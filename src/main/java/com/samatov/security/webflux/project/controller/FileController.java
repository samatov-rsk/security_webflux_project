package com.samatov.security.webflux.project.controller;

import com.samatov.security.webflux.project.dto.FileDTO;
import com.samatov.security.webflux.project.exception.NotFoundException;
import com.samatov.security.webflux.project.mapper.FileMapper;
import com.samatov.security.webflux.project.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "File Controller", description = "Endpoints for managing files")
public class FileController {
    private final FileService fileService;
    private final FileMapper fileMapper;


    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @PostMapping("/upload")
    @Operation(summary = "Upload a file", description = "Uploads a new file and returns its details")
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
    @Operation(summary = "Get file by ID", description = "Returns the file with the specified ID")
    public Mono<ResponseEntity<FileDTO>> getFileById(@PathVariable Long id) {
        return fileService.getFileById(id)
                .map(fileEntity -> ResponseEntity.ok(fileMapper.map(fileEntity)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/all")
    @Operation(summary = "Get all files", description = "Returns a list of all files")
    public Flux<FileDTO> getAllFiles() {
        return fileService.getAllFiles()
                .map(fileMapper::map);
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file by ID", description = "Deletes the file with the specified ID")
    public Mono<ResponseEntity<Void>> deleteFile(@PathVariable Long id) {
        return fileService.deleteFile(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.<Void>notFound().build()));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/download/{filename}")
    @Operation(summary = "Download file by name", description = "Downloads the file with the specified filename")
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
