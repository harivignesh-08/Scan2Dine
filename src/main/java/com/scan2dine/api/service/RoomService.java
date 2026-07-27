package com.scan2dine.api.service;

import com.scan2dine.api.dto.request.RoomRequest;
import com.scan2dine.api.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {
    RoomResponse createRoom(RoomRequest request);
    List<RoomResponse> getAllRooms();
    List<RoomResponse> getRoomsByHostel(Long hostelId);
    RoomResponse getRoomById(Long id);
    RoomResponse updateRoom(Long id, RoomRequest request);
    void deleteRoom(Long id);
}
