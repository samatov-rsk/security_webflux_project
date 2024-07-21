package com.samatov.security.webflux.project.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    private Integer id;

    @NotNull
    @Column("user_id")
    private Integer userId;

    @NotNull
    @Column("file_id")
    private Integer fileId;

    @NotNull
    @MappedCollection(idColumn = "user_id")
    private Mono<User> user;

    @NotNull
    @MappedCollection(idColumn = "file_id")
    private Mono<File> file;
}
