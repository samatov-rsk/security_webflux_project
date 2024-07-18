package com.samatov.security.webflux.project.repository;

import com.samatov.security.webflux.project.model.File;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface FileRepository extends R2dbcRepository<File, Long> {
}
