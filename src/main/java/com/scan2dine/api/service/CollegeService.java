package com.scan2dine.api.service;

import com.scan2dine.api.dto.request.CollegeRequest;
import com.scan2dine.api.dto.request.RegisterRequest;
import com.scan2dine.api.dto.response.CollegeResponse;

import java.util.List;

public interface CollegeService {
    CollegeResponse registerCollege(RegisterRequest request);
    CollegeResponse approveCollege(Long id);
    CollegeResponse suspendCollege(Long id);
    List<CollegeResponse> getAllColleges();
    CollegeResponse getCollegeById(Long id);
    CollegeResponse updateCollege(Long id, CollegeRequest request);
    void deleteCollege(Long id);
}
