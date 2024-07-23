package com.samatov.security.webflux.project.mapper;

import com.samatov.security.webflux.project.dto.FileDTO;
import com.samatov.security.webflux.project.model.FileEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileEntity map(FileDTO file);

    @InheritInverseConfiguration
    FileDTO map(FileEntity file);
}
