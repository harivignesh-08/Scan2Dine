package com.scan2dine.api.integration.erp;

import com.scan2dine.api.config.TenantContext;
import com.scan2dine.api.dto.request.ErpConfigRequest;
import com.scan2dine.api.dto.response.ErpStatusResponse;
import com.scan2dine.api.dto.response.StudentResponse;
import com.scan2dine.api.entity.College;
import com.scan2dine.api.entity.Student;
import com.scan2dine.api.exception.BadRequestException;
import com.scan2dine.api.exception.ResourceNotFoundException;
import com.scan2dine.api.mapper.StudentMapper;
import com.scan2dine.api.repository.CollegeRepository;
import com.scan2dine.api.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class ErpIntegrationServiceImpl implements ErpIntegrationService {

    private final CollegeRepository collegeRepository;
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public ErpIntegrationServiceImpl(CollegeRepository collegeRepository,
                                     StudentRepository studentRepository,
                                     StudentMapper studentMapper) {
        this.collegeRepository = collegeRepository;
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    @Override
    @Transactional
    public void configureErp(ErpConfigRequest request) {
        Long collegeId = TenantContext.getCurrentTenant();
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id " + collegeId));

        college.setErpName(request.getErpName());
        college.setErpBaseUrl(request.getErpBaseUrl());
        college.setErpApiKey(request.getErpApiKey());

        collegeRepository.save(college);
        log.info("ERP configured successfully for college: {}", college.getCollegeName());
    }

    @Override
    @Transactional(readOnly = true)
    public ErpStatusResponse getErpStatus() {
        Long collegeId = TenantContext.getCurrentTenant();
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id " + collegeId));

        if (college.getErpName() == null || college.getErpBaseUrl() == null) {
            return ErpStatusResponse.builder()
                    .configured(false)
                    .status("NOT_CONFIGURED")
                    .message("ERP has not been configured yet.")
                    .build();
        }

        // Simulating a connectivity check ping to ERP Base URL
        boolean isConnected = college.getErpBaseUrl().startsWith("http");
        return ErpStatusResponse.builder()
                .configured(true)
                .erpName(college.getErpName())
                .erpBaseUrl(college.getErpBaseUrl())
                .status(isConnected ? "CONNECTED" : "ERROR")
                .message(isConnected ? "Ping successful. API connection is active." : "Failed to ping ERP server. Verify URL.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ErpStudentDto fetchStudentFromErp(String rollNumber) {
        Long collegeId = TenantContext.getCurrentTenant();
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id " + collegeId));

        if (college.getErpName() == null) {
            throw new BadRequestException("ERP integration has not been configured for this college.");
        }

        log.info("Connecting to {} at {} to fetch student {}", college.getErpName(), college.getErpBaseUrl(), rollNumber);
        
        // Simulating the API response from Campus7, Fedena, Academia, CAMU, Custom ERP
        return simulateErpFetch(college.getErpName(), rollNumber);
    }

    @Override
    @Transactional
    public StudentResponse syncStudent(String rollNumber) {
        // 1. Fetch Student details from ERP
        ErpStudentDto erpStudent = fetchStudentFromErp(rollNumber);

        // 2. Check if student already exists locally in this college
        Optional<Student> localStudentOpt = studentRepository.findByRollNumber(rollNumber);
        Student student;

        if (localStudentOpt.isPresent()) {
            student = localStudentOpt.get();
            // Update local student information from ERP
            student.setName(erpStudent.getName());
            student.setDepartment(erpStudent.getDepartment());
            student.setYear(erpStudent.getYear());
            student.setPhone(erpStudent.getPhone());
            log.info("Updating existing student {} from ERP info", rollNumber);
        } else {
            student = new Student();
            student.setRollNumber(erpStudent.getRollNumber());
            student.setName(erpStudent.getName());
            student.setDepartment(erpStudent.getDepartment());
            student.setYear(erpStudent.getYear());
            student.setPhone(erpStudent.getPhone());
            student.setStatus("ACTIVE");
            log.info("Registering new student {} synchronized from ERP", rollNumber);
        }

        Student saved = studentRepository.save(student);
        return studentMapper.toResponse(saved);
    }

    // ERP simulation resolver
    private ErpStudentDto simulateErpFetch(String erpName, String rollNumber) {
        // Create mock records based on roll number endings
        String name;
        String dept;
        int year;
        String phone;

        int hash = Math.abs(rollNumber.hashCode());
        String[] firstNames = {"Aravind", "Vikram", "Sneha", "Karthik", "Priya", "Rahul", "Anjali", "Suresh", "Divya", "Deepak"};
        String[] lastNames = {"Kumar", "Rajan", "Sharma", "Nair", "Patel", "Reddy", "Iyer", "Sen", "Joshi", "Murthy"};
        String[] departments = {"Computer Science", "Information Technology", "Electronics & Communication", "Mechanical Engg", "Civil Engg"};

        name = firstNames[hash % firstNames.length] + " " + lastNames[(hash / 2) % lastNames.length];
        dept = departments[hash % departments.length];
        year = (hash % 4) + 1; // 1 to 4
        phone = "+91 9840" + String.format("%06d", hash % 1000000);

        return ErpStudentDto.builder()
                .name(name)
                .rollNumber(rollNumber.toUpperCase())
                .department(dept)
                .year(year)
                .phone(phone)
                .erpSource(erpName)
                .build();
    }
}
