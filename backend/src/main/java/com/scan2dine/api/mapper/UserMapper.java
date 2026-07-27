package com.scan2dine.api.mapper;

import com.scan2dine.api.dto.request.UserRequest;
import com.scan2dine.api.dto.response.UserResponse;
import com.scan2dine.api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole());
        res.setActive(user.getActive());
        return res;
    }

    public void updateEntity(UserRequest req, User user) {
        if (req == null || user == null) return;
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setRole(req.getRole());
    }
}
