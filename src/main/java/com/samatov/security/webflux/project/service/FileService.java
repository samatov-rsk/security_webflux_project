package com.samatov.security.webflux.project.service;

import com.samatov.security.webflux.project.model.File;
import com.samatov.security.webflux.project.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final UserService userService;
    private final EventService eventService;

    public Mono<File> getById(Long id) {
        return fileRepository.findById(id)
                .doOnSuccess(file -> log.info("Файл найден : {}", file))
                .doOnError(error -> log.error("Файл не найден : {}", error.getMessage()));
    }

    public Flux<File> getAll() {
        return fileRepository.findAll()
                .doOnNext(file -> log.info("Файл получены"))
                .doOnError(error -> log.error("Ошибка при получении файлов"));
    }

    public Mono<File> save(File file) {
        return fileRepository.save(file)
                .doOnSuccess(file1 -> log.info("Файл успешно сохранен : {}", file1))
                .doOnError(error -> log.error("Ошибка при сохранении файла"));
    }

    public Mono<Void> deleteById(Long id) {
        return fileRepository.deleteById(id)
                .doOnSuccess(file -> log.info("Файл успешно удален"))
                .doOnError(error -> log.error("Ошибка при удалении файла"));
    }

}
