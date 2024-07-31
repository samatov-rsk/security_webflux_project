package com.samatov.security.webflux.project.utils;

import com.samatov.security.webflux.project.dto.*;
import com.samatov.security.webflux.project.enums.Role;
import com.samatov.security.webflux.project.enums.Status;
import com.samatov.security.webflux.project.model.Event;
import com.samatov.security.webflux.project.model.FileEntity;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.security.PBFDK2Encoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

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
                .id(1L)
                .userId(1L)
                .fileId(1L)
                .build();
    }

    public static EventDTO createEventDTO() {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(1L);
        eventDTO.setUser(createUserDTO());
        eventDTO.setFile(createFileDTO());
        return eventDTO;
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
