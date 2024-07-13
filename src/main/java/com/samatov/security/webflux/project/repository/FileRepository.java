package com.samatov.security.webflux.project.repository;

import com.samatov.security.webflux.project.model.FileEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface FileRepository extends ReactiveCrudRepository<FileEntity, Long> {
}
