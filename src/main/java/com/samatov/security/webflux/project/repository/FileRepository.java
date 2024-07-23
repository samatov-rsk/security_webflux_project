package com.samatov.security.webflux.project.repository;

import com.samatov.security.webflux.project.model.FileEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends R2dbcRepository<FileEntity, Long> {
}
