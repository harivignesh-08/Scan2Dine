package com.scan2dine.api.mapper;

import com.scan2dine.api.dto.request.RoomRequest;
import com.scan2dine.api.dto.response.RoomResponse;
import com.scan2dine.api.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomResponse toResponse(Room room) {
        if (room == null) return null;
        RoomResponse res = new RoomResponse();
        res.setId(room.getId());
        res.setHostelId(room.getHostel().getId());
        res.setHostelName(room.getHostel().getName());
        res.setRoomNumber(room.getRoomNumber());
        res.setCapacity(room.getCapacity());
        return res;
    }

    public void updateEntity(RoomRequest req, Room room) {
        if (req == null || room == null) return;
        room.setRoomNumber(req.getRoomNumber());
        room.setCapacity(req.getCapacity());
    }
}
