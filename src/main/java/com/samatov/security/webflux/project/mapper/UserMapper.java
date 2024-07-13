package com.samatov.security.webflux.project.mapper;

import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.model.UserEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(UserEntity user);

    @InheritInverseConfiguration
    UserEntity toEntity(UserDTO userDTO);
}
