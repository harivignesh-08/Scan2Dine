package com.scan2dine.api.service;

import com.scan2dine.api.dto.request.HostelRequest;
import com.scan2dine.api.dto.response.HostelResponse;

import java.util.List;

public interface HostelService {
    HostelResponse createHostel(HostelRequest request);
    List<HostelResponse> getAllHostels();
    HostelResponse getHostelById(Long id);
    HostelResponse updateHostel(Long id, HostelRequest request);
    void deleteHostel(Long id);
}
