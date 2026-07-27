package com.scan2dine.api.service.impl;

import com.scan2dine.api.dto.request.StudentRequest;
import com.scan2dine.api.dto.response.StudentResponse;
import com.scan2dine.api.entity.Hostel;
import com.scan2dine.api.entity.Room;
import com.scan2dine.api.entity.Student;
import com.scan2dine.api.exception.BadRequestException;
import com.scan2dine.api.exception.ResourceNotFoundException;
import com.scan2dine.api.mapper.StudentMapper;
import com.scan2dine.api.repository.HostelRepository;
import com.scan2dine.api.repository.RoomRepository;
import com.scan2dine.api.repository.StudentRepository;
import com.scan2dine.api.service.StudentService;
import com.scan2dine.api.config.TenantContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final HostelRepository hostelRepository;
    private final RoomRepository roomRepository;
    private final StudentMapper studentMapper;

    public StudentServiceImpl(StudentRepository studentRepository,
                              HostelRepository hostelRepository,
                              RoomRepository roomRepository,
                              StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.hostelRepository = hostelRepository;
        this.roomRepository = roomRepository;
        this.studentMapper = studentMapper;
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.findByRollNumber(request.getRollNumber()).isPresent()) {
            throw new BadRequestException("Student with roll number " + request.getRollNumber() + " already exists.");
        }
        if (request.getBarcode() != null && !request.getBarcode().isBlank() && 
                studentRepository.findByBarcode(request.getBarcode()).isPresent()) {
            throw new BadRequestException("Barcode " + request.getBarcode() + " is already linked to another student.");
        }

        Student student = new Student();
        studentMapper.updateEntity(request, student);

        resolveHostelAndRoom(request, student);

        Student savedStudent = studentRepository.save(student);
        return studentMapper.toResponse(savedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
        return studentMapper.toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentByRollNumber(String rollNumber) {
        Student student = studentRepository.findByRollNumber(rollNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with roll number " + rollNumber));
        return studentMapper.toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentByBarcode(String barcode) {
        Student student = studentRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with barcode " + barcode));
        return studentMapper.toResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));

        // Check roll number unique mapping
        if (!student.getRollNumber().equals(request.getRollNumber()) && 
                studentRepository.findByRollNumber(request.getRollNumber()).isPresent()) {
            throw new BadRequestException("Student with roll number " + request.getRollNumber() + " already exists.");
        }

        // Check barcode unique mapping
        if (request.getBarcode() != null && !request.getBarcode().isBlank() && 
                !request.getBarcode().equals(student.getBarcode()) && 
                studentRepository.findByBarcode(request.getBarcode()).isPresent()) {
            throw new BadRequestException("Barcode " + request.getBarcode() + " is already linked to another student.");
        }

        studentMapper.updateEntity(request, student);
        resolveHostelAndRoom(request, student);

        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id " + id);
        }
        studentRepository.deleteById(id);
    }

    private void resolveHostelAndRoom(StudentRequest request, Student student) {
        if (request.getHostelId() != null) {
            Hostel hostel = hostelRepository.findById(request.getHostelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with id " + request.getHostelId()));
            student.setHostel(hostel);

            if (request.getRoomId() != null) {
                Room room = roomRepository.findById(request.getRoomId())
                        .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + request.getRoomId()));
                
                // Validate if room belongs to the selected hostel
                if (!room.getHostel().getId().equals(hostel.getId())) {
                    throw new BadRequestException("Room " + room.getRoomNumber() + " does not belong to hostel " + hostel.getName());
                }
                student.setRoom(room);
            } else {
                student.setRoom(null);
            }
        } else {
            student.setHostel(null);
            student.setRoom(null);
        }
    }

    @Override
    @Transactional
    public String importStudents(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BadRequestException("Invalid filename");
        }

        int created = 0;
        int updated = 0;

        try {
            if (filename.endsWith(".csv")) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                    String line;
                    boolean isHeader = true;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        
                        String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                        
                        if (isHeader) {
                            isHeader = false;
                            if (data[0].toLowerCase().contains("name") || data[0].toLowerCase().contains("roll")) {
                                continue; 
                            }
                        }

                        String name = cleanCsvValue(data, 0);
                        String rollNumber = cleanCsvValue(data, 1);
                        String department = cleanCsvValue(data, 2);
                        String yearStr = cleanCsvValue(data, 3);
                        String phone = cleanCsvValue(data, 4);
                        String hostelName = cleanCsvValue(data, 5);
                        String roomNumber = cleanCsvValue(data, 6);
                        String barcode = cleanCsvValue(data, 7);

                        if (rollNumber.isEmpty()) continue;

                        int year = 1;
                        try {
                            year = Integer.parseInt(yearStr);
                        } catch (NumberFormatException e) {}

                        boolean isNew = saveOrUpdateImportedStudent(name, rollNumber, department, year, phone, hostelName, roomNumber, barcode);
                        if (isNew) created++; else updated++;
                    }
                }
            } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                    Sheet sheet = workbook.getSheetAt(0);
                    for (Row row : sheet) {
                        if (row.getRowNum() == 0) {
                            Cell cell = row.getCell(0);
                            if (cell != null && cell.getCellType() == CellType.STRING) {
                                String val = cell.getStringCellValue().toLowerCase();
                                if (val.contains("name") || val.contains("roll")) {
                                    continue;
                                }
                            }
                        }

                        String name = getCellValueAsString(row.getCell(0));
                        String rollNumber = getCellValueAsString(row.getCell(1));
                        String department = getCellValueAsString(row.getCell(2));
                        String yearStr = getCellValueAsString(row.getCell(3));
                        String phone = getCellValueAsString(row.getCell(4));
                        String hostelName = getCellValueAsString(row.getCell(5));
                        String roomNumber = getCellValueAsString(row.getCell(6));
                        String barcode = getCellValueAsString(row.getCell(7));

                        if (rollNumber.isEmpty()) continue;

                        int year = 1;
                        try {
                            year = (int) Double.parseDouble(yearStr);
                        } catch (NumberFormatException e) {
                            try {
                                year = Integer.parseInt(yearStr);
                            } catch (Exception ex) {}
                        }

                        boolean isNew = saveOrUpdateImportedStudent(name, rollNumber, department, year, phone, hostelName, roomNumber, barcode);
                        if (isNew) created++; else updated++;
                    }
                }
            } else {
                throw new BadRequestException("Unsupported file format. Please upload CSV or Excel (.xlsx/.xls) file.");
            }
        } catch (IOException e) {
            throw new BadRequestException("Error reading import file: " + e.getMessage());
        }

        return "Import complete. Successfully created " + created + " and updated " + updated + " student records.";
    }

    private String cleanCsvValue(String[] data, int index) {
        if (index >= data.length || data[index] == null) return "";
        String val = data[index].trim();
        if (val.startsWith("\"") && val.endsWith("\"")) {
            val = val.substring(1, val.length() - 1).trim();
        }
        return val;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                if (val == (long) val) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private boolean saveOrUpdateImportedStudent(String name, String rollNumber, String department, int year, 
                                                String phone, String hostelName, String roomNumber, String barcode) {
        Optional<Student> existingOpt = studentRepository.findByRollNumber(rollNumber);
        Student student;
        boolean isNew = false;

        if (existingOpt.isPresent()) {
            student = existingOpt.get();
        } else {
            student = new Student();
            student.setRollNumber(rollNumber);
            student.setCollegeId(TenantContext.getCurrentTenant());
            isNew = true;
        }

        if (!name.isEmpty()) student.setName(name);
        if (!department.isEmpty()) student.setDepartment(department);
        student.setYear(year);
        if (!phone.isEmpty()) student.setPhone(phone);
        if (!barcode.isEmpty()) student.setBarcode(barcode);
        student.setStatus("ACTIVE");

        if (!hostelName.isEmpty()) {
            Optional<Hostel> hostelOpt = hostelRepository.findByName(hostelName);
            if (hostelOpt.isPresent()) {
                Hostel hostel = hostelOpt.get();
                student.setHostel(hostel);

                if (!roomNumber.isEmpty()) {
                    Optional<Room> roomOpt = roomRepository.findByHostelIdAndRoomNumber(hostel.getId(), roomNumber);
                    if (roomOpt.isPresent()) {
                        student.setRoom(roomOpt.get());
                    }
                }
            }
        }

        studentRepository.save(student);
        return isNew;
    }
}
