package com.scan2dine.api.service.impl;

import com.scan2dine.api.barcode.BarcodeValidator;
import com.scan2dine.api.dto.request.BarcodeRegistrationRequest;
import com.scan2dine.api.dto.request.BarcodeScanRequest;
import com.scan2dine.api.dto.response.BarcodeScanResponse;
import com.scan2dine.api.entity.*;
import com.scan2dine.api.exception.BadRequestException;
import com.scan2dine.api.exception.ResourceNotFoundException;
import com.scan2dine.api.repository.*;
import com.scan2dine.api.security.CustomUserDetails;
import com.scan2dine.api.service.BarcodeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class BarcodeServiceImpl implements BarcodeService {

    private final StudentRepository studentRepository;
    private final BarcodeRegistrationRepository barcodeRegistrationRepository;
    private final MealRepository mealRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final BarcodeValidator barcodeValidator;

    public BarcodeServiceImpl(StudentRepository studentRepository,
                              BarcodeRegistrationRepository barcodeRegistrationRepository,
                              MealRepository mealRepository,
                              AttendanceRepository attendanceRepository,
                              UserRepository userRepository,
                              NotificationRepository notificationRepository,
                              BarcodeValidator barcodeValidator) {
        this.studentRepository = studentRepository;
        this.barcodeRegistrationRepository = barcodeRegistrationRepository;
        this.mealRepository = mealRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.barcodeValidator = barcodeValidator;
    }

    @Override
    @Transactional
    public void registerBarcode(BarcodeRegistrationRequest request) {
        if (!barcodeValidator.isValid(request.getBarcodeValue())) {
            throw new BadRequestException("Invalid barcode format. Alphanumeric values between 5 and 50 characters required.");
        }

        Student student = studentRepository.findByRollNumber(request.getRollNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with roll number: " + request.getRollNumber()));

        if (student.getBarcode() != null && student.getBarcode().equals(request.getBarcodeValue())) {
            throw new BadRequestException("Student already has this barcode registered.");
        }

        // Verify if barcode is already taken
        Optional<Student> studentWithBarcode = studentRepository.findByBarcode(request.getBarcodeValue());
        if (studentWithBarcode.isPresent()) {
            throw new BadRequestException("Barcode is already mapped to student: " + studentWithBarcode.get().getName());
        }

        // Update Student barcode field
        student.setBarcode(request.getBarcodeValue());
        studentRepository.save(student);

        // Get logged in user details for audit
        User registeredBy = getCurrentUser();

        // Save Barcode Registration Log
        BarcodeRegistration registration = new BarcodeRegistration();
        registration.setStudent(student);
        registration.setBarcodeValue(request.getBarcodeValue());
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setRegisteredBy(registeredBy);
        
        barcodeRegistrationRepository.save(registration);
    }

    @Override
    @Transactional
    public BarcodeScanResponse scanBarcode(BarcodeScanRequest request) {
        String barcode = request.getBarcodeValue().trim();
        LocalDateTime scanTime = LocalDateTime.now();
        LocalDate today = scanTime.toLocalDate();
        LocalTime timeNow = scanTime.toLocalTime();

        // 1. Check active meal timing
        List<Meal> meals = mealRepository.findAll();
        Meal activeMeal = null;
        for (Meal meal : meals) {
            LocalTime start = meal.getStartTime();
            LocalTime end = meal.getEndTime();
            if ((timeNow.isAfter(start) || timeNow.equals(start)) && 
                (timeNow.isBefore(end) || timeNow.equals(end))) {
                activeMeal = meal;
                break;
            }
        }

        if (activeMeal == null) {
            return BarcodeScanResponse.builder()
                    .status("ACCESS_DENIED")
                    .message("Access Denied: No dining meal session is currently active at this time (" + timeNow + ").")
                    .scanTime(scanTime)
                    .build();
        }

        // 2. Find Student using Barcode
        Optional<Student> studentOpt = studentRepository.findByBarcode(barcode);
        if (studentOpt.isEmpty()) {
            return BarcodeScanResponse.builder()
                    .status("ACCESS_DENIED")
                    .message("Access Denied: Barcode is not linked to any registered student.")
                    .mealName(activeMeal.getMealName())
                    .scanTime(scanTime)
                    .build();
        }

        Student student = studentOpt.get();
        User warden = getCurrentUser();

        // 3. Check Student Active
        if (!"ACTIVE".equalsIgnoreCase(student.getStatus())) {
            return BarcodeScanResponse.builder()
                    .status("ACCESS_DENIED")
                    .message("Access Denied: Student account is suspended/inactive.")
                    .studentName(student.getName())
                    .rollNumber(student.getRollNumber())
                    .mealName(activeMeal.getMealName())
                    .scanTime(scanTime)
                    .build();
        }

        // 4. Check Hostel Registration
        if (student.getHostel() == null) {
            return BarcodeScanResponse.builder()
                    .status("ACCESS_DENIED")
                    .message("Access Denied: Student is not registered in any Hostel.")
                    .studentName(student.getName())
                    .rollNumber(student.getRollNumber())
                    .mealName(activeMeal.getMealName())
                    .scanTime(scanTime)
                    .build();
        }

        // 5. Check Duplicate Scan
        boolean duplicateExists = attendanceRepository.existsByStudentIdAndMealIdAndAttendanceDate(
                student.getId(), activeMeal.getId(), today);

        if (duplicateExists) {
            // Save as DUPLICATE_ATTEMPT attendance record
            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setMeal(activeMeal);
            attendance.setScanTime(scanTime);
            attendance.setAttendanceDate(today);
            attendance.setStatus("DUPLICATE_ATTEMPT");
            attendance.setWarden(warden);
            attendanceRepository.save(attendance);

            // Log tenant notification
            Notification notification = new Notification();
            notification.setTitle("Duplicate Scan Alert");
            notification.setMessage("Student " + student.getName() + " (" + student.getRollNumber() + 
                    ") attempted to scan twice for " + activeMeal.getMealName() + ".");
            notificationRepository.save(notification);

            return BarcodeScanResponse.builder()
                    .status("ACCESS_DENIED")
                    .message("Access Denied: Duplicate scan attempt. Already recorded for " + activeMeal.getMealName() + " today.")
                    .studentName(student.getName())
                    .rollNumber(student.getRollNumber())
                    .hostelName(student.getHostel().getName())
                    .roomNumber(student.getRoom() != null ? student.getRoom().getRoomNumber() : "N/A")
                    .mealName(activeMeal.getMealName())
                    .scanTime(scanTime)
                    .build();
        }

        // 6. Access Granted: Save Attendance
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setMeal(activeMeal);
        attendance.setScanTime(scanTime);
        attendance.setAttendanceDate(today);
        attendance.setStatus("PRESENT");
        attendance.setWarden(warden);
        attendanceRepository.save(attendance);

        return BarcodeScanResponse.builder()
                .status("ACCESS_GRANTED")
                .message("Access Granted: Dining entry recorded for " + activeMeal.getMealName() + ".")
                .studentName(student.getName())
                .rollNumber(student.getRollNumber())
                .hostelName(student.getHostel().getName())
                .roomNumber(student.getRoom() != null ? student.getRoom().getRoomNumber() : "N/A")
                .mealName(activeMeal.getMealName())
                .scanTime(scanTime)
                .build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userRepository.findById(userDetails.getId()).orElse(null);
        }
        return null;
    }
}
