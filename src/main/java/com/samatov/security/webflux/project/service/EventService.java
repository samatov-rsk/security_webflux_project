package com.samatov.security.webflux.project.service;

import com.samatov.security.webflux.project.mapper.EventMapper;
import com.samatov.security.webflux.project.model.Event;
import com.samatov.security.webflux.project.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public Mono<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public Flux<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Mono<Event> saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public Mono<Event> createEvent(Long userId, Long fileId) {
        Event event = Event.builder()
                .userId(userId)
                .fileId(fileId)
                .build();
        return eventRepository.save(event);
    }
}
