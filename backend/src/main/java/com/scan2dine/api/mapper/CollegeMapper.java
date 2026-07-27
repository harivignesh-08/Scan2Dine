package com.scan2dine.api.mapper;

import com.scan2dine.api.dto.request.CollegeRequest;
import com.scan2dine.api.dto.response.CollegeResponse;
import com.scan2dine.api.entity.College;
import org.springframework.stereotype.Component;

@Component
public class CollegeMapper {

    public CollegeResponse toResponse(College college) {
        if (college == null) return null;
        CollegeResponse res = new CollegeResponse();
        res.setId(college.getId());
        res.setCollegeName(college.getCollegeName());
        res.setCollegeCode(college.getCollegeCode());
        res.setLogo(college.getLogo());
        res.setThemeColor(college.getThemeColor());
        res.setEmail(college.getEmail());
        res.setPhone(college.getPhone());
        res.setErpName(college.getErpName());
        res.setErpBaseUrl(college.getErpBaseUrl());
        res.setErpApiKey(college.getErpApiKey());
        res.setSubscriptionPlan(college.getSubscriptionPlan());
        res.setStatus(college.getStatus());
        res.setSubscriptionStartDate(college.getSubscriptionStartDate());
        res.setSubscriptionEndDate(college.getSubscriptionEndDate());
        res.setCreatedAt(college.getCreatedAt());
        return res;
    }

    public void updateEntity(CollegeRequest req, College college) {
        if (req == null || college == null) return;
        college.setCollegeName(req.getCollegeName());
        college.setEmail(req.getEmail());
        college.setPhone(req.getPhone());
        if (req.getLogo() != null) college.setLogo(req.getLogo());
        if (req.getThemeColor() != null) college.setThemeColor(req.getThemeColor());
        if (req.getErpName() != null) college.setErpName(req.getErpName());
        if (req.getErpBaseUrl() != null) college.setErpBaseUrl(req.getErpBaseUrl());
        if (req.getErpApiKey() != null) college.setErpApiKey(req.getErpApiKey());
        if (req.getSubscriptionPlan() != null) college.setSubscriptionPlan(req.getSubscriptionPlan());
        if (req.getStatus() != null) college.setStatus(req.getStatus());
        if (req.getSubscriptionStartDate() != null) college.setSubscriptionStartDate(req.getSubscriptionStartDate());
        if (req.getSubscriptionEndDate() != null) college.setSubscriptionEndDate(req.getSubscriptionEndDate());
    }
}
