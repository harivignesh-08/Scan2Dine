package com.scan2dine.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HostelResponse {
    private Long id;
    private String name;
    private Integer capacity;
}
