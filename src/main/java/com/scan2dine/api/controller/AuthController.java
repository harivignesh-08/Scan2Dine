package com.scan2dine.api.controller;

import com.scan2dine.api.dto.request.LoginRequest;
import com.scan2dine.api.dto.request.RegisterRequest;
import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.AuthResponse;
import com.scan2dine.api.dto.response.CollegeResponse;
import com.scan2dine.api.entity.College;
import com.scan2dine.api.exception.BadRequestException;
import com.scan2dine.api.repository.CollegeRepository;
import com.scan2dine.api.repository.UserRepository;
import com.scan2dine.api.security.CustomUserDetails;
import com.scan2dine.api.security.JwtTokenProvider;
import com.scan2dine.api.service.CollegeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for College Registration and User Logins")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CollegeService collegeService;
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          CollegeService collegeService,
                          UserRepository userRepository,
                          CollegeRepository collegeRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.collegeService = collegeService;
        this.userRepository = userRepository;
        this.collegeRepository = collegeRepository;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new College Tenant alongside its College Admin account")
    public ResponseEntity<ApiResponse<CollegeResponse>> registerCollege(@Valid @RequestBody RegisterRequest request) {
        CollegeResponse response = collegeService.registerCollege(request);
        return ResponseEntity.ok(ApiResponse.success("College registered successfully. Verification pending by Super Admin.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login as Super Admin, College Admin, or Warden to obtain a JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // If user is College Admin or Warden, verify college subscription status
        Long collegeId = userDetails.getCollegeId();
        String collegeName = null;
        String logo = null;
        String themeColor = null;
        String subscriptionPlan = "FREE";

        if (collegeId != null) {
            College college = collegeRepository.findById(collegeId)
                    .orElseThrow(() -> new BadRequestException("College context not found for user."));
            
            if ("SUSPENDED".equalsIgnoreCase(college.getStatus())) {
                throw new BadRequestException("This college account has been suspended. Please contact the administrator.");
            }
            if ("PENDING".equalsIgnoreCase(college.getStatus())) {
                throw new BadRequestException("This college registration is pending approval by the Super Admin.");
            }
            
            collegeName = college.getCollegeName();
            logo = college.getLogo();
            themeColor = college.getThemeColor();
            subscriptionPlan = college.getSubscriptionPlan();
        }

        // Generate JWT token with claims
        String token = tokenProvider.generateToken(authentication, userDetails.getId(), collegeId, userDetails.getRole());

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRole())
                .collegeId(collegeId)
                .collegeName(collegeName)
                .logo(logo)
                .themeColor(themeColor)
                .subscriptionPlan(subscriptionPlan)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Authentication successful.", authResponse));
    }
}
