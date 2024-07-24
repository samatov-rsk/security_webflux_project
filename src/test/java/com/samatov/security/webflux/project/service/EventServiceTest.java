package com.samatov.security.webflux.project.service;

import com.samatov.security.webflux.project.exception.EventException;
import com.samatov.security.webflux.project.exception.NotFoundException;
import com.samatov.security.webflux.project.model.Event;
import com.samatov.security.webflux.project.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Когда getEventById вызывается, то успешный результат")
    void getEventByIdTest() {
        Event event = new Event();
        when(eventRepository.findById(anyLong())).thenReturn(Mono.just(event));

        Mono<Event> result = eventService.getEventById(1L);

        assertEquals(event, result.block());
        verify(eventRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Когда getEventById вызывается, то NotFoundException")
    void getEventByIdNotFoundTest() {
        when(eventRepository.findById(anyLong())).thenReturn(Mono.empty());

        assertThrows(NotFoundException.class, () -> eventService.getEventById(1L).block());
        verify(eventRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Когда getAllEvents вызывается, то успешный результат")
    void getAllEventsTest() {
        Event event = new Event();
        when(eventRepository.findAll()).thenReturn(Flux.just(event));

        Flux<Event> result = eventService.getAllEvents();

        assertTrue(result.collectList().block().contains(event));
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Когда getAllEvents вызывается, то EventException")
    void getAllEventsNotFoundTest() {
        when(eventRepository.findAll()).thenReturn(Flux.empty());

        assertThrows(EventException.class, () -> eventService.getAllEvents().collectList().block());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Когда saveEvent вызывается, то успешный результат")
    void saveEventTest() {
        Event event = new Event();
        when(eventRepository.save(any(Event.class))).thenReturn(Mono.just(event));

        Mono<Event> result = eventService.saveEvent(event);

        assertEquals(event, result.block());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Когда createEvent вызывается, то успешный результат")
    void createEventTest() {
        Event event = new Event();
        when(eventRepository.save(any(Event.class))).thenReturn(Mono.just(event));

        Mono<Event> result = eventService.createEvent(1L, 1L);

        assertEquals(event, result.block());
        verify(eventRepository, times(1)).save(any(Event.class));
    }
}
