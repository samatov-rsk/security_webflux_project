package com.samatov.security.webflux.project.controller;

import com.samatov.security.webflux.project.dto.EventDTO;
import com.samatov.security.webflux.project.dto.FileDTO;
import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.mapper.EventMapper;
import com.samatov.security.webflux.project.mapper.FileMapper;
import com.samatov.security.webflux.project.mapper.UserMapper;
import com.samatov.security.webflux.project.model.Event;
import com.samatov.security.webflux.project.service.EventService;
import com.samatov.security.webflux.project.service.FileService;
import com.samatov.security.webflux.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
@Validated
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final FileService fileService;
    private final EventMapper eventMapper;
    private final FileMapper fileMapper;
    private final UserMapper userMapper;

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<EventDTO>> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .flatMap(event -> Mono.zip(
                        Mono.just(event),
                        userService.getUserById(event.getUserId()),  // Получение пользователя по ID
                        fileService.getFileById(event.getFileId()))  // Получение файла по ID
                )
                .map(tuple -> {
                    Event event = tuple.getT1();
                    UserDTO userDTO = userMapper.map(tuple.getT2());
                    FileDTO fileDTO = fileMapper.map(tuple.getT3());
                    EventDTO eventDTO = eventMapper.map(event);
                    eventDTO.setUser(userDTO);
                    eventDTO.setFile(fileDTO);
                    return ResponseEntity.ok(eventDTO);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/all")
    public Flux<EventDTO> getAllEvents() {
        return eventService.getAllEvents()
                .flatMap(event -> Mono.zip(
                        Mono.just(event),
                        userService.getUserById(event.getUserId()),  // Получение пользователя по ID
                        fileService.getFileById(event.getFileId()))  // Получение файла по ID
                )
                .map(tuple -> {
                    Event event = tuple.getT1();
                    UserDTO userDTO = userMapper.map(tuple.getT2());
                    FileDTO fileDTO = fileMapper.map(tuple.getT3());
                    EventDTO eventDTO = eventMapper.map(event);
                    eventDTO.setUser(userDTO);
                    eventDTO.setFile(fileDTO);
                    return eventDTO;
                });
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    public Mono<ResponseEntity<EventDTO>> createEvent(@Validated @RequestBody EventDTO eventDTO) {
        Event event = eventMapper.map(eventDTO);
        return eventService.saveEvent(event)
                .map(eventMapper::map)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
}