package com.scan2dine.api.repository;

import com.scan2dine.api.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollegeRepository extends JpaRepository<College, Long> {
    Optional<College> findByCollegeCode(String collegeCode);
}
