package com.samatov.security.webflux.project.utils;

import com.samatov.security.webflux.project.dto.*;
import com.samatov.security.webflux.project.enums.Role;
import com.samatov.security.webflux.project.enums.Status;
import com.samatov.security.webflux.project.model.Event;
import com.samatov.security.webflux.project.model.FileEntity;
import com.samatov.security.webflux.project.model.User;

import java.util.Date;

public class TestUtils {

    public static User createUser() {
        return User.builder()
                .username("rus")
                .password("password")
                .roles(Role.USER)
                .status(Status.ACTIVE)
                .build();
    }

    public static User createAdminUser() {
        return User.builder()
                .username("admin")
                .password("password")
                .roles(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();
    }

    public static UserDTO createUserDTO() {
        return UserDTO.builder()
                .username("testuser")
                .password("password")
                .build();
    }

    public static Event createEvent() {
        return Event.builder()
                .userId(1L)
                .fileId(1L)
                .build();
    }

    public static Event createEvent(Long userId, Long fileId) {
        return Event.builder()
                .userId(userId)
                .fileId(fileId)
                .build();
    }

    public static EventDTO createEventDTO(UserDTO userDTO, FileDTO fileDTO) {
        return EventDTO.builder()
                .id(1L)
                .user(userDTO)
                .file(fileDTO)
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

    public static FileEntity createFileEntity() {
        return FileEntity.builder()
                .name("test" + System.currentTimeMillis() + ".txt")
                .location("s3://bucket/test" + System.currentTimeMillis() + ".txt")
                .status(Status.ACTIVE)
                .build();
    }

    public static FileEntity createFileEntityForFileController() {
        return FileEntity.builder()
                .id(1L)
                .name("test" + System.currentTimeMillis() + ".txt")
                .location("s3://bucket/test" + System.currentTimeMillis() + ".txt")
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

    public static UserDTO createUserDTOEvents(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .status(user.getStatus())
                .password(null)
                .build();
    }

    public static FileDTO createFileDTOEvents(FileEntity fileEntity) {
        return FileDTO.builder()
                .id(fileEntity.getId())
                .name(fileEntity.getName())
                .location(fileEntity.getLocation())
                .status(fileEntity.getStatus())
                .build();
    }

}
