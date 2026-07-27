package com.scan2dine.api.service;

import com.scan2dine.api.dto.request.UserRequest;
import com.scan2dine.api.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    List<UserResponse> getAllUsers();
    List<UserResponse> getWardensByCollege();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserRequest request);
    UserResponse toggleUserStatus(Long id);
    void deleteUser(Long id);
}
