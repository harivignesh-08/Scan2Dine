package com.scan2dine.api.repository;

import com.scan2dine.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(String role);
    long countByRole(String role);
    
    // Fallback search that can bypass the tenant filter in raw SQL if needed, but since 
    // filter is disabled when TenantContext is empty, a standard count is sufficient.
}
