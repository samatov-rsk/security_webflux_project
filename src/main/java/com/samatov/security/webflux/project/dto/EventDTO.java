package com.samatov.security.webflux.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EventDTO {

    private Long id;
    private Long userId;
    private Long fileId;
}
