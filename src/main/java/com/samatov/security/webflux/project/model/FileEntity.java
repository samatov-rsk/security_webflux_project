package com.samatov.security.webflux.project.model;

import com.samatov.security.webflux.project.enums.Status;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;

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
