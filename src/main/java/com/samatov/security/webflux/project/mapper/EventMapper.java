package com.samatov.security.webflux.project.mapper;

import com.samatov.security.webflux.project.dto.EventDTO;
import com.samatov.security.webflux.project.model.EventEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventEntity toEventEntity(EventDTO event);

    @InheritInverseConfiguration
    EventDTO toEventDTO(EventEntity event);
}
