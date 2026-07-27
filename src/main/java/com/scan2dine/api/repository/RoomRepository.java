package com.scan2dine.api.repository;

import com.scan2dine.api.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHostelId(Long hostelId);
    Optional<Room> findByHostelIdAndRoomNumber(Long hostelId, String roomNumber);
}
