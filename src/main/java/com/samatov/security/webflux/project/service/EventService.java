package com.samatov.security.webflux.project.service;

import com.samatov.security.webflux.project.exception.EventException;
import com.samatov.security.webflux.project.exception.NotFoundException;
import com.samatov.security.webflux.project.model.Event;
import com.samatov.security.webflux.project.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Mono<Event> getEventById(Long id) {
        return eventRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Событие с таким id не найдено")))
                .doOnSuccess(event -> log.info("Получение события по id: {}", id))
                .doOnError(error -> log.error("Событие с id {} не найдено", id, error));
    }

    public Flux<Event> getAllEvents() {
        return eventRepository.findAll()
                .switchIfEmpty(Mono.error(new EventException("События не найдены")))
                .doOnNext(event -> log.info("Получение всех событий: {}", event))
                .doOnError(error -> log.error("Ошибка при получении всех событий", error));
    }

    public Mono<Event> saveEvent(Event event) {
        return eventRepository.save(event)
                .doOnSuccess(savedEvent -> log.info("Событие успешно сохранено: {}", savedEvent))
                .doOnError(error -> log.error("Ошибка при сохранении события", error));
    }

    public Mono<Event> createEvent(Long userId, Long fileId) {
        Event event = Event.builder()
                .userId(userId)
                .fileId(fileId)
                .build();
        return eventRepository.save(event)
                .doOnSuccess(savedEvent -> log.info("Событие создано: userId={}, fileId={}", userId, fileId))
                .doOnError(error -> log.error("Ошибка при создании события", error));
    }
}
