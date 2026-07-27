package com.scan2dine.api.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.scan2dine.api.dto.response.AttendanceResponse;
import com.scan2dine.api.entity.Attendance;
import com.scan2dine.api.repository.AttendanceRepository;
import com.scan2dine.api.service.ReportService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final AttendanceRepository attendanceRepository;

    public ReportServiceImpl(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getDailyReport(LocalDate date) {
        return attendanceRepository.findByAttendanceDateOrderByScanTimeDesc(date).stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getMonthlyReport(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return attendanceRepository.findByAttendanceDateBetweenOrderByScanTimeDesc(start, end).stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generatePdfReport(LocalDate startDate, LocalDate endDate) {
        List<AttendanceResponse> records = attendanceRepository.findByAttendanceDateBetweenOrderByScanTimeDesc(startDate, endDate)
                .stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Styling colors
            Color brandColor = new Color(79, 70, 229); // Indigo #4F46E5

            // Main Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, brandColor);
            Paragraph title = new Paragraph("Scan2Dine - Meal Attendance Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Subtitle with date ranges
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Paragraph subtitle = new Paragraph("Report Period: " + startDate.format(DateTimeFormatter.ISO_DATE) + 
                    " to " + endDate.format(DateTimeFormatter.ISO_DATE), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" ")); // Spacing

            // Table Structure
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2.5f, 2f, 2f, 1.5f, 3f, 2f});

            // Column Headers
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            String[] headers = {"S.No", "Student Name", "Roll No", "Hostel", "Meal", "Scan Time", "Status"};
            
            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, headFont));
                headerCell.setBackgroundColor(brandColor);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerCell.setPadding(6);
                table.addCell(headerCell);
            }

            // Populate records
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            int serialNo = 1;
            
            for (AttendanceResponse record : records) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(serialNo++), cellFont)));
                table.addCell(new PdfPCell(new Phrase(record.getStudentName(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(record.getRollNumber(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(record.getHostelName(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(record.getMealName(), cellFont)));
                
                String scanStr = record.getScanTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                table.addCell(new PdfPCell(new Phrase(scanStr, cellFont)));

                // Style Status cell
                PdfPCell statusCell = new PdfPCell(new Phrase(record.getStatus(), cellFont));
                if ("DUPLICATE_ATTEMPT".equalsIgnoreCase(record.getStatus())) {
                    statusCell.setBackgroundColor(new Color(254, 226, 226)); // light red
                } else {
                    statusCell.setBackgroundColor(new Color(220, 252, 231)); // light green
                }
                table.addCell(statusCell);
            }

            document.add(table);
            document.close();

        } catch (DocumentException ex) {
            log.error("PDF generation failed: ", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateExcelReport(LocalDate startDate, LocalDate endDate) {
        List<AttendanceResponse> records = attendanceRepository.findByAttendanceDateBetweenOrderByScanTimeDesc(startDate, endDate)
                .stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance Records");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Columns definition
            String[] columns = {"ID", "Student Name", "Roll Number", "Department", "Hostel", "Room", "Meal", "Scan Time", "Status", "Warden"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate rows
            int rowIdx = 1;
            for (AttendanceResponse record : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(record.getId());
                row.createCell(1).setCellValue(record.getStudentName());
                row.createCell(2).setCellValue(record.getRollNumber());
                row.createCell(3).setCellValue(record.getDepartment());
                row.createCell(4).setCellValue(record.getHostelName());
                row.createCell(5).setCellValue(record.getRoomNumber());
                row.createCell(6).setCellValue(record.getMealName());
                
                String scanStr = record.getScanTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                row.createCell(7).setCellValue(scanStr);
                row.createCell(8).setCellValue(record.getStatus());
                row.createCell(9).setCellValue(record.getWardenUsername());
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException ex) {
            log.error("Excel generation failed: ", ex);
            throw new RuntimeException("Failed to generate Excel report", ex);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReportServiceImpl.class);

    private AttendanceResponse mapToAttendanceResponse(Attendance attendance) {
        AttendanceResponse res = new AttendanceResponse();
        res.setId(attendance.getId());
        res.setStudentId(attendance.getStudent().getId());
        res.setStudentName(attendance.getStudent().getName());
        res.setRollNumber(attendance.getStudent().getRollNumber());
        res.setDepartment(attendance.getStudent().getDepartment());
        res.setHostelName(attendance.getStudent().getHostel() != null ? attendance.getStudent().getHostel().getName() : "N/A");
        res.setRoomNumber(attendance.getStudent().getRoom() != null ? attendance.getStudent().getRoom().getRoomNumber() : "N/A");
        res.setMealName(attendance.getMeal().getMealName());
        res.setScanTime(attendance.getScanTime());
        res.setAttendanceDate(attendance.getAttendanceDate());
        res.setStatus(attendance.getStatus());
        res.setWardenUsername(attendance.getWarden() != null ? attendance.getWarden().getUsername() : "System");
        return res;
    }
}
