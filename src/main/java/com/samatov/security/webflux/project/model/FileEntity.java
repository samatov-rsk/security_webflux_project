package com.samatov.security.webflux.project.model;

import com.samatov.security.webflux.project.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Table("files")
public class FileEntity {

    @Id
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String location;

    @NotNull
    private Status status;
}
