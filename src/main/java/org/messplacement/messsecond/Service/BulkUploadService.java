package org.messplacement.messsecond.Service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.messplacement.messsecond.DTO.BulkUploadResult;
import org.messplacement.messsecond.Entities.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Parses bulk attendance files (CSV or Excel) and delegates to MessService
 * to upsert each row using the same logic as the single-record endpoint.
 *
 * Expected column order: reg | date (YYYY-MM-DD) | breakfast | lunch | dinner
 * Boolean values: "true"/"1"/"yes" (case-insensitive) count as present.
 */
@Service
public class BulkUploadService {

    @Autowired
    private MessService messService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ── Entry point ────────────────────────────────────────────────────────

    public BulkUploadResult processFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
            return processExcel(file);
        }
        return processCsv(file);
    }

    // ── CSV parser ─────────────────────────────────────────────────────────

    private BulkUploadResult processCsv(MultipartFile file) {
        BulkUploadResult result = new BulkUploadResult();
        int rowNum = 0;
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] header = reader.readNext(); // skip header row
            if (header == null) { result.addError(0, "File is empty"); return result; }

            String[] row;
            while ((row = reader.readNext()) != null) {
                rowNum++;
                try {
                    if (row.length < 5) {
                        result.addError(rowNum, "Row has fewer than 5 columns (expected: reg, date, breakfast, lunch, dinner)");
                        continue;
                    }
                    Student s = parseRow(row[0], row[1], row[2], row[3], row[4]);
                    messService.addStudent(List.of(s));
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(rowNum, e.getMessage());
                }
            }
        } catch (IOException | CsvValidationException e) {
            result.addError(rowNum, "File read error: " + e.getMessage());
        }
        return result;
    }

    // ── Excel parser ───────────────────────────────────────────────────────

    private BulkUploadResult processExcel(MultipartFile file) {
        BulkUploadResult result = new BulkUploadResult();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 0;
            for (Row row : sheet) {
                if (rowNum++ == 0) continue; // skip header
                try {
                    String reg       = cellStr(row.getCell(0));
                    String date      = cellStr(row.getCell(1));
                    String breakfast = cellStr(row.getCell(2));
                    String lunch     = cellStr(row.getCell(3));
                    String dinner    = cellStr(row.getCell(4));
                    Student s = parseRow(reg, date, breakfast, lunch, dinner);
                    messService.addStudent(List.of(s));
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(rowNum, e.getMessage());
                }
            }
        } catch (IOException e) {
            result.addError(0, "File read error: " + e.getMessage());
        }
        return result;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Student parseRow(String reg, String dateStr, String breakfast,
                             String lunch, String dinner) {
        if (reg == null || reg.isBlank())
            throw new IllegalArgumentException("Missing registration number");

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: '" + dateStr + "' (expected YYYY-MM-DD)");
        }

        Student s = new Student();
        s.setreg(reg.trim());
        s.setDate(date);
        s.setBreakfast(toBoolean(breakfast));
        s.setLunch(toBoolean(lunch));
        s.setDinner(toBoolean(dinner));
        return s;
    }

    private boolean toBoolean(String value) {
        if (value == null) return false;
        String v = value.trim().toLowerCase();
        return v.equals("true") || v.equals("1") || v.equals("yes");
    }

    private String cellStr(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> {
                // Excel stores dates as numeric serials — check before treating as number
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString(); // YYYY-MM-DD
                }
                double d = cell.getNumericCellValue();
                yield String.valueOf((long) d);
            }
            case STRING  -> cell.getStringCellValue();
            default      -> "";
        };
    }
}
