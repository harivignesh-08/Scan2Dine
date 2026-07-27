package com.scan2dine.api.repository;

import com.scan2dine.api.entity.BarcodeRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarcodeRegistrationRepository extends JpaRepository<BarcodeRegistration, Long> {
    Optional<BarcodeRegistration> findByBarcodeValue(String barcodeValue);
    Optional<BarcodeRegistration> findByStudentId(Long studentId);
}
