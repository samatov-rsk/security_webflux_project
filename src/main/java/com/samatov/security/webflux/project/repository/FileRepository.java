package com.samatov.security.webflux.project.repository;

import com.samatov.security.webflux.project.model.File;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface FileRepository extends ReactiveCrudRepository<File, Long> {
}
