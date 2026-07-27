package com.scan2dine.api.service;

import com.scan2dine.api.dto.request.StudentRequest;
import com.scan2dine.api.dto.response.StudentResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface StudentService {
    StudentResponse createStudent(StudentRequest request);
    List<StudentResponse> getAllStudents();
    StudentResponse getStudentById(Long id);
    StudentResponse getStudentByRollNumber(String rollNumber);
    StudentResponse getStudentByBarcode(String barcode);
    StudentResponse updateStudent(Long id, StudentRequest request);
    void deleteStudent(Long id);
    String importStudents(MultipartFile file);
}
