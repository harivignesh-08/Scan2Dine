package com.scan2dine.api.controller;

import com.scan2dine.api.dto.request.StudentRequest;
import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.StudentResponse;
import com.scan2dine.api.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Student Management", description = "Endpoints for managing hostel student profiles")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @Operation(summary = "Register a student profile in the local system manually")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.ok(ApiResponse.success("Student created successfully.", response));
    }

    @GetMapping
    @Operation(summary = "Get list of all students registered under the current college tenant")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents() {
        List<StudentResponse> response = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a student profile details by system ID")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success("Student retrieved successfully.", response));
    }

    @GetMapping("/roll/{rollNumber}")
    @Operation(summary = "Get a student profile details by Roll Number")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByRollNumber(@PathVariable String rollNumber) {
        StudentResponse response = studentService.getStudentByRollNumber(rollNumber);
        return ResponseEntity.ok(ApiResponse.success("Student retrieved successfully.", response));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get student details by scanned Barcode value")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByBarcode(@PathVariable String barcode) {
        StudentResponse response = studentService.getStudentByBarcode(barcode);
        return ResponseEntity.ok(ApiResponse.success("Student retrieved successfully.", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student profile details (Name, Hostel, Room assignments)")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a student profile record")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully."));
    }

    @PostMapping("/import")
    @Operation(summary = "Import student records in bulk from a CSV or Excel file")
    public ResponseEntity<ApiResponse<String>> importStudents(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String result = studentService.importStudents(file);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
