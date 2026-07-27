package com.scan2dine.api.mapper;

import com.scan2dine.api.dto.request.HostelRequest;
import com.scan2dine.api.dto.response.HostelResponse;
import com.scan2dine.api.entity.Hostel;
import org.springframework.stereotype.Component;

@Component
public class HostelMapper {

    public HostelResponse toResponse(Hostel hostel) {
        if (hostel == null) return null;
        HostelResponse res = new HostelResponse();
        res.setId(hostel.getId());
        res.setName(hostel.getName());
        res.setCapacity(hostel.getCapacity());
        return res;
    }

    public void updateEntity(HostelRequest req, Hostel hostel) {
        if (req == null || hostel == null) return;
        hostel.setName(req.getName());
        hostel.setCapacity(req.getCapacity());
    }
}
