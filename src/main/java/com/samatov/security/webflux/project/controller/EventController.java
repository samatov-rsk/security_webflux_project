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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Event Controller", description = "Endpoints for managing events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final FileService fileService;
    private final EventMapper eventMapper;
    private final FileMapper fileMapper;
    private final UserMapper userMapper;

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Returns the event with the specified ID")
    public Mono<ResponseEntity<EventDTO>> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .flatMap(event -> Mono.zip(
                        Mono.just(event),
                        userService.getUserById(event.getUserId()),
                        fileService.getFileById(event.getFileId()))
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
    @Operation(summary = "Get all events", description = "Returns a list of all events")
    public Flux<EventDTO> getAllEvents() {
        return eventService.getAllEvents()
                .flatMap(event -> Mono.zip(
                        Mono.just(event),
                        userService.getUserById(event.getUserId()),
                        fileService.getFileById(event.getFileId()))
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
}