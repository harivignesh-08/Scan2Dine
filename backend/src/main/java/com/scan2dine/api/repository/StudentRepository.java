package com.scan2dine.api.repository;

import com.scan2dine.api.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByBarcode(String barcode);
    Optional<Student> findByRollNumber(String rollNumber);
    List<Student> findByStatus(String status);
}
