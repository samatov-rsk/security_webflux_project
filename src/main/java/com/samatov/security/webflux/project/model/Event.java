package com.samatov.security.webflux.project.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("events")
public class Event {

    @Id
    private Long id;

    @NotNull
    @Column("user_id")
    private Long userId;

    @NotNull
    @Column("file_id")
    private Long fileId;
}
