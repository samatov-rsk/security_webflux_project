package com.samatov.security.webflux.project.controller;

import com.samatov.security.webflux.project.dto.EventDTO;
import com.samatov.security.webflux.project.mapper.EventMapper;
import com.samatov.security.webflux.project.service.EventService;
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
    private final EventMapper eventMapper;

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<EventDTO>> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(eventMapper::map)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/all")
    public Flux<ResponseEntity<EventDTO>> getAllEvents() {
        return eventService.getAllEvents()
                .map(eventMapper::map)
                .map(ResponseEntity::ok);
    }
}
