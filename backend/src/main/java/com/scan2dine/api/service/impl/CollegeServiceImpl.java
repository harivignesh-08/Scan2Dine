package com.scan2dine.api.service.impl;

import com.scan2dine.api.dto.request.CollegeRequest;
import com.scan2dine.api.dto.request.RegisterRequest;
import com.scan2dine.api.dto.response.CollegeResponse;
import com.scan2dine.api.entity.College;
import com.scan2dine.api.entity.User;
import com.scan2dine.api.exception.BadRequestException;
import com.scan2dine.api.exception.ResourceNotFoundException;
import com.scan2dine.api.mapper.CollegeMapper;
import com.scan2dine.api.repository.CollegeRepository;
import com.scan2dine.api.repository.UserRepository;
import com.scan2dine.api.service.CollegeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollegeServiceImpl implements CollegeService {

    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;
    private final CollegeMapper collegeMapper;
    private final PasswordEncoder passwordEncoder;

    public CollegeServiceImpl(CollegeRepository collegeRepository,
                              UserRepository userRepository,
                              CollegeMapper collegeMapper,
                              PasswordEncoder passwordEncoder) {
        this.collegeRepository = collegeRepository;
        this.userRepository = userRepository;
        this.collegeMapper = collegeMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CollegeResponse registerCollege(RegisterRequest request) {
        if (collegeRepository.findByCollegeCode(request.getCollegeCode()).isPresent()) {
            throw new BadRequestException("College with code " + request.getCollegeCode() + " already exists.");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username " + request.getUsername() + " is already taken.");
        }

        // 1. Create and Save College
        College college = new College();
        college.setCollegeName(request.getCollegeName());
        college.setCollegeCode(request.getCollegeCode());
        college.setEmail(request.getEmail());
        college.setPhone(request.getPhone());
        college.setLogo(request.getLogo());
        college.setThemeColor(request.getThemeColor() != null ? request.getThemeColor() : "#4F46E5");
        college.setErpName(request.getErpName());
        college.setErpBaseUrl(request.getErpBaseUrl());
        college.setErpApiKey(request.getErpApiKey());
        college.setSubscriptionPlan("FREE");
        college.setStatus("PENDING"); // Super Admin must approve
        
        College savedCollege = collegeRepository.save(college);

        // 2. Create and Save College Admin User
        User adminUser = new User();
        adminUser.setCollegeId(savedCollege.getId());
        adminUser.setUsername(request.getUsername());
        adminUser.setPassword(passwordEncoder.encode(request.getPassword()));
        adminUser.setEmail(request.getEmail());
        adminUser.setRole("COLLEGE_ADMIN");
        adminUser.setActive(true);

        userRepository.save(adminUser);

        return collegeMapper.toResponse(savedCollege);
    }

    @Override
    @Transactional
    public CollegeResponse approveCollege(Long id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id " + id));
        college.setStatus("APPROVED");
        return collegeMapper.toResponse(collegeRepository.save(college));
    }

    @Override
    @Transactional
    public CollegeResponse suspendCollege(Long id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id " + id));
        college.setStatus("SUSPENDED");
        return collegeMapper.toResponse(collegeRepository.save(college));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeResponse> getAllColleges() {
        return collegeRepository.findAll().stream()
                .map(collegeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeResponse getCollegeById(Long id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id " + id));
        return collegeMapper.toResponse(college);
    }

    @Override
    @Transactional
    public CollegeResponse updateCollege(Long id, CollegeRequest request) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id " + id));
        collegeMapper.updateEntity(request, college);
        return collegeMapper.toResponse(collegeRepository.save(college));
    }

    @Override
    @Transactional
    public void deleteCollege(Long id) {
        if (!collegeRepository.existsById(id)) {
            throw new ResourceNotFoundException("College not found with id " + id);
        }
        collegeRepository.deleteById(id);
    }
}
