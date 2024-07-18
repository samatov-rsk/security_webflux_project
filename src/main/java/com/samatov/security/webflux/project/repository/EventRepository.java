package com.samatov.security.webflux.project.repository;

import com.samatov.security.webflux.project.model.Event;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface EventRepository extends R2dbcRepository<Event, Long> {
}
