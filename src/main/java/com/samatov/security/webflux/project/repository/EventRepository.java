package com.samatov.security.webflux.project.repository;

import com.samatov.security.webflux.project.model.EventEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface EventRepository extends R2dbcRepository<EventEntity, Long> {
}
