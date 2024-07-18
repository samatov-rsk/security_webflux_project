package com.samatov.security.webflux.project.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "files")
public class File {

    @Id
    private Integer id;

    @NotNull
    @Column("name")
    private String name;

    @NotNull
    @Column("file_path")
    private String filePath;
}