package com.samatov.security.webflux.project.mapper;

import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.model.User;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO map(User user);

    @InheritInverseConfiguration
    User map(UserDTO userDTO);
}
