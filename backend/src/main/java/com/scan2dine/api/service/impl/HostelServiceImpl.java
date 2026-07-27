package com.scan2dine.api.service.impl;

import com.scan2dine.api.dto.request.HostelRequest;
import com.scan2dine.api.dto.response.HostelResponse;
import com.scan2dine.api.entity.Hostel;
import com.scan2dine.api.exception.BadRequestException;
import com.scan2dine.api.exception.ResourceNotFoundException;
import com.scan2dine.api.mapper.HostelMapper;
import com.scan2dine.api.repository.HostelRepository;
import com.scan2dine.api.service.HostelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HostelServiceImpl implements HostelService {

    private final HostelRepository hostelRepository;
    private final HostelMapper hostelMapper;

    public HostelServiceImpl(HostelRepository hostelRepository, HostelMapper hostelMapper) {
        this.hostelRepository = hostelRepository;
        this.hostelMapper = hostelMapper;
    }

    @Override
    @Transactional
    public HostelResponse createHostel(HostelRequest request) {
        if (hostelRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Hostel with name " + request.getName() + " already exists.");
        }

        Hostel hostel = new Hostel();
        hostelMapper.updateEntity(request, hostel);

        Hostel saved = hostelRepository.save(hostel);
        return hostelMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HostelResponse> getAllHostels() {
        return hostelRepository.findAll().stream()
                .map(hostelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HostelResponse getHostelById(Long id) {
        Hostel hostel = hostelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with id " + id));
        return hostelMapper.toResponse(hostel);
    }

    @Override
    @Transactional
    public HostelResponse updateHostel(Long id, HostelRequest request) {
        Hostel hostel = hostelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with id " + id));

        if (!hostel.getName().equalsIgnoreCase(request.getName()) && 
                hostelRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Hostel with name " + request.getName() + " already exists.");
        }

        hostelMapper.updateEntity(request, hostel);
        return hostelMapper.toResponse(hostelRepository.save(hostel));
    }

    @Override
    @Transactional
    public void deleteHostel(Long id) {
        if (!hostelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hostel not found with id " + id);
        }
        hostelRepository.deleteById(id);
    }
}
