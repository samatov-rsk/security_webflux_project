package com.samatov.security.webflux.project.model;

import com.samatov.security.webflux.project.enums.Role;
import com.samatov.security.webflux.project.enums.Status;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Table("users")
public class UserEntity {

    @Id
    private Long id;
    private String username;
    private String password;
    private List<Role> roles;
    private Status status;


    @ToString.Include(name = "password")
    private String maskPassword() {
        return "***********";
    }
}

