package com.samatov.security.webflux.project.utils;

import com.samatov.security.webflux.project.dto.AuthRequestDTO;
import com.samatov.security.webflux.project.dto.AuthResponseDTO;
import com.samatov.security.webflux.project.dto.FileDTO;
import com.samatov.security.webflux.project.dto.UserDTO;
import com.samatov.security.webflux.project.enums.Role;
import com.samatov.security.webflux.project.enums.Status;
import com.samatov.security.webflux.project.model.FileEntity;
import com.samatov.security.webflux.project.model.User;

import java.util.Date;

public class TestUtils {

    public static User createUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .roles(Role.USER)
                .status(Status.ACTIVE)
                .build();
    }

    public static UserDTO createUserDTO() {
        return UserDTO.builder()
                .id(1L)
                .username("testuser")
                .roles(Role.USER)
                .status(Status.ACTIVE)
                .build();
    }

    public static AuthRequestDTO createAuthRequestDTO() {
        return AuthRequestDTO.builder()
                .username("testuser")
                .password("password")
                .build();
    }

    public static AuthResponseDTO createAuthResponseDTO() {
        return AuthResponseDTO.builder()
                .userId(1L)
                .token("dummy-token")
                .issuerDate(new Date())
                .expiresDate(new Date(System.currentTimeMillis() + 3600000))
                .build();
    }

    public static User createUpdatedUser(User user) {
        return User.builder()
                .id(user.getId())
                .username("newUsername")
                .password(user.getPassword())
                .roles(Role.ADMIN)
                .status(Status.INACTIVE)
                .build();
    }

    public static UserDTO createUpdatedUserDTO(UserDTO userDTO) {
        return UserDTO.builder()
                .id(userDTO.getId())
                .username("newUsername")
                .password(userDTO.getPassword())
                .roles(Role.ADMIN)
                .status(Status.INACTIVE)
                .build();
    }

    public static FileEntity createFileEntity() {
        return FileEntity.builder()
                .id(1L)
                .name("test.txt")
                .location("s3://bucket/test.txt")
                .status(Status.ACTIVE)
                .build();
    }

    public static FileDTO createFileDTO() {
        return FileDTO.builder()
                .id(1L)
                .name("test.txt")
                .location("s3://bucket/test.txt")
                .status(Status.ACTIVE)
                .build();
    }
}
