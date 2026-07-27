package com.scan2dine.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomResponse {
    private Long id;
    private Long hostelId;
    private String hostelName;
    private String roomNumber;
    private Integer capacity;
}
