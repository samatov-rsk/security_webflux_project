package com.samatov.security.webflux.project.mapper;

import com.samatov.security.webflux.project.dto.EventDTO;
import com.samatov.security.webflux.project.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "fileId", target = "file.id")
    EventDTO map(Event event);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "file.id", target = "fileId")
    Event map(EventDTO eventDTO);
}