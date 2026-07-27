package com.scan2dine.api.service.impl;

import com.scan2dine.api.dto.request.RoomRequest;
import com.scan2dine.api.dto.response.RoomResponse;
import com.scan2dine.api.entity.Hostel;
import com.scan2dine.api.entity.Room;
import com.scan2dine.api.exception.BadRequestException;
import com.scan2dine.api.exception.ResourceNotFoundException;
import com.scan2dine.api.mapper.RoomMapper;
import com.scan2dine.api.repository.HostelRepository;
import com.scan2dine.api.repository.RoomRepository;
import com.scan2dine.api.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HostelRepository hostelRepository;
    private final RoomMapper roomMapper;

    public RoomServiceImpl(RoomRepository roomRepository,
                           HostelRepository hostelRepository,
                           RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.hostelRepository = hostelRepository;
        this.roomMapper = roomMapper;
    }

    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        Hostel hostel = hostelRepository.findById(request.getHostelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with id " + request.getHostelId()));

        if (roomRepository.findByHostelIdAndRoomNumber(request.getHostelId(), request.getRoomNumber()).isPresent()) {
            throw new BadRequestException("Room number " + request.getRoomNumber() + " already exists in hostel " + hostel.getName());
        }

        Room room = new Room();
        room.setHostel(hostel);
        roomMapper.updateEntity(request, room);

        Room saved = roomRepository.save(room);
        return roomMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByHostel(Long hostelId) {
        if (!hostelRepository.existsById(hostelId)) {
            throw new ResourceNotFoundException("Hostel not found with id " + hostelId);
        }
        return roomRepository.findByHostelId(hostelId).stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + id));
        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + id));

        Hostel hostel = hostelRepository.findById(request.getHostelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with id " + request.getHostelId()));

        if (!room.getRoomNumber().equalsIgnoreCase(request.getRoomNumber()) && 
                roomRepository.findByHostelIdAndRoomNumber(request.getHostelId(), request.getRoomNumber()).isPresent()) {
            throw new BadRequestException("Room number " + request.getRoomNumber() + " already exists in hostel " + hostel.getName());
        }

        room.setHostel(hostel);
        roomMapper.updateEntity(request, room);

        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with id " + id);
        }
        roomRepository.deleteById(id);
    }
}
