package com.samatov.security.webflux.project.mapper;

import com.samatov.security.webflux.project.dto.EventDTO;
import com.samatov.security.webflux.project.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventDTO map(Event event);

    @Mapping(target = "id", ignore = true)
    Event map(EventDTO eventDTO);
}
