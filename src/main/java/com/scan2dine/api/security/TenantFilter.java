package com.scan2dine.api.security;

import com.scan2dine.api.config.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                if (userDetails.getCollegeId() != null) {
                    TenantContext.setCurrentTenant(userDetails.getCollegeId());
                }
            } else {
                // Fallback for registration or public APIs where the college context is passed via header
                String tenantHeader = request.getHeader("X-Tenant-ID");
                if (StringUtils.hasText(tenantHeader)) {
                    try {
                        TenantContext.setCurrentTenant(Long.parseLong(tenantHeader));
                    } catch (NumberFormatException e) {
                        // Ignore invalid header format
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            // Always clean up thread local to prevent memory leak / crosstalk
            TenantContext.clear();
        }
    }
}
