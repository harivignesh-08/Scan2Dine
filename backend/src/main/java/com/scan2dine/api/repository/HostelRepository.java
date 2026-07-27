package com.scan2dine.api.repository;

import com.scan2dine.api.entity.Hostel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostelRepository extends JpaRepository<Hostel, Long> {
    Optional<Hostel> findByName(String name);
}
