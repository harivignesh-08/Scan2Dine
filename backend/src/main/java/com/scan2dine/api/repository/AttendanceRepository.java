package com.scan2dine.api.repository;

import com.scan2dine.api.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByStudentIdAndMealIdAndAttendanceDate(Long studentId, Long mealId, LocalDate attendanceDate);

    List<Attendance> findByAttendanceDateOrderByScanTimeDesc(LocalDate date);

    List<Attendance> findByStudentIdOrderByScanTimeDesc(Long studentId);

    List<Attendance> findByAttendanceDateBetweenOrderByScanTimeDesc(LocalDate startDate, LocalDate endDate);

    long countByAttendanceDateAndMealIdAndStatus(LocalDate date, Long mealId, String status);

    long countByAttendanceDateAndStatus(LocalDate date, String status);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND a.meal.mealName = :mealName AND a.status = :status")
    long countTodayScanByMealNameAndStatus(@Param("date") LocalDate date, @Param("mealName") String mealName, @Param("status") String status);

    long countByAttendanceDate(LocalDate date);
}
