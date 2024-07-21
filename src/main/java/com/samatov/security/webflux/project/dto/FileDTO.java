package com.samatov.security.webflux.project.dto;

import com.samatov.security.webflux.project.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FileDTO {
    private Long id;
    private String name;
    private String location;
    private Status status;
}
