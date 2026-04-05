package org.messplacement.messsecond;

import org.messplacement.messsecond.DTO.BulkUploadResult;
import org.messplacement.messsecond.DTO.StudentDue;
import org.messplacement.messsecond.Entities.Student;
import org.messplacement.messsecond.Service.BulkUploadService;
import org.messplacement.messsecond.Service.MessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://localhost",
        "https://mess-application-front-end-angular.vercel.app"
})
public class Controller {

    @Autowired
    MessService messService;

    @Autowired
    BulkUploadService bulkUploadService;

    // ── Read endpoints ─────────────────────────────────────────────────────

    @GetMapping("/home")
    public String getHomePage() {
        return messService.getHomePage();
    }

    @GetMapping("/getStudents")
    public List<Student> getStudents(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return this.messService.getStudents(date);
    }

    @GetMapping("/students/dues")
    public List<StudentDue> getTotalDues(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return this.messService.getTotalDues(startDate, endDate);
    }

    @GetMapping("/students/{Reg}")
    public List<Student> getStudent(@PathVariable String Reg) {
        return messService.getStudent(Reg);
    }

    @GetMapping("studentTotal/{reg}")
    public int getStudentTotal(@PathVariable String reg) {
        return messService.getStudentTotal(reg);
    }

    // ── Write endpoints (Admin only) ───────────────────────────────────────

    @PostMapping("/students")
    public ResponseEntity<String> addStudent(@RequestBody List<Student> students) {
        try {
            messService.addStudent(students);
            return new ResponseEntity<>("Student(s) added successfully", HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(
                    "Error: A student entry with this registration number and date already exists.",
                    HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "An unexpected error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Bulk upload endpoint — accepts CSV or XLSX file, processes each row using
     * the same upsert logic as addStudent, and returns a per-row result summary.
     *
     * BulkUploadService is injected here rather than in a separate controller
     * because it is logically a student-data write operation, consistent with
     * the rest of the endpoints in this controller.
     */
    @PostMapping(value = "/students/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkUploadResult> bulkUpload(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String name = file.getOriginalFilename();
        boolean validType = name != null &&
                (name.endsWith(".csv") || name.endsWith(".xlsx") || name.endsWith(".xls"));
        if (!validType) {
            BulkUploadResult err = new BulkUploadResult();
            err.addError(0, "Only .csv, .xlsx, and .xls files are accepted.");
            return ResponseEntity.badRequest().body(err);
        }
        return ResponseEntity.ok(bulkUploadService.processFile(file));
    }

    @PutMapping("/students")
    public String updateStudent(@RequestBody Student student) {
        messService.updateStudent(student);
        return "Student updated";
    }

    @DeleteMapping("/students/{reg}/{date}")
    public String deleteStudent(@PathVariable String reg, @PathVariable String date) {
        messService.deleteStudent(reg, LocalDate.parse(date));
        return "Student deleted";
    }
}
