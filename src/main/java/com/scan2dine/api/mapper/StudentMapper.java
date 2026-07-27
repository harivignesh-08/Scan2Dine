package com.scan2dine.api.mapper;

import com.scan2dine.api.dto.request.StudentRequest;
import com.scan2dine.api.dto.response.StudentResponse;
import com.scan2dine.api.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentResponse toResponse(Student student) {
        if (student == null) return null;
        StudentResponse res = new StudentResponse();
        res.setId(student.getId());
        res.setName(student.getName());
        res.setRollNumber(student.getRollNumber());
        res.setDepartment(student.getDepartment());
        res.setYear(student.getYear());
        res.setPhone(student.getPhone());
        res.setBarcode(student.getBarcode());
        res.setStatus(student.getStatus());
        
        if (student.getHostel() != null) {
            res.setHostelId(student.getHostel().getId());
            res.setHostelName(student.getHostel().getName());
        }
        if (student.getRoom() != null) {
            res.setRoomId(student.getRoom().getId());
            res.setRoomNumber(student.getRoom().getRoomNumber());
        }
        return res;
    }

    public void updateEntity(StudentRequest req, Student student) {
        if (req == null || student == null) return;
        student.setName(req.getName());
        student.setRollNumber(req.getRollNumber());
        student.setDepartment(req.getDepartment());
        student.setYear(req.getYear());
        student.setPhone(req.getPhone());
        student.setBarcode(req.getBarcode());
        if (req.getStatus() != null) {
            student.setStatus(req.getStatus());
        }
    }
}
