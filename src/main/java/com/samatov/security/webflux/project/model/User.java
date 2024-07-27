package com.samatov.security.webflux.project.model;

import com.samatov.security.webflux.project.enums.Role;
import com.samatov.security.webflux.project.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Table("users")
public class User {

    @Id
    private Long id;

    @NotNull
    @Column("username")
    private String username;

    @NotNull
    @Column("password")
    private String password;

    @NotNull
    @Column("roles")
    private Role roles;

    @NotNull
    @Column("status")
    private Status status;

    @ToString.Include(name = "password")
    private String maskPassword() {
        return "***********";
    }
}

