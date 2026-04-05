package org.messplacement.messsecond;

import org.messplacement.messsecond.Dao.MealPriceRepository;
import org.messplacement.messsecond.Dao.WeeklyMenuRepository;
import org.messplacement.messsecond.DTO.BulkUploadResult;
import org.messplacement.messsecond.DTO.StudentDue;
import org.messplacement.messsecond.Entities.MealPrice;
import org.messplacement.messsecond.Entities.Student;
import org.messplacement.messsecond.Entities.WeeklyMenu;
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

/**
 * Unified application controller — handles all data endpoints.
 *
 * Auth endpoints (/auth/**) are intentionally kept in AuthController because
 * they belong to a completely different concern (identity / token issuance)
 * and use SecurityConfig.permitAll() rules that differ from every other route.
 *
 * Endpoint map:
 *
 *  ── Students (all authenticated) ─────────────────────────────────────────
 *  GET  /getStudents?date=             → attendance list for a date        (ADMIN|GUEST)
 *  GET  /students/dues?startDate&endDate → dues report                     (ADMIN|GUEST)
 *  GET  /students/{reg}                → student attendance history        (ADMIN|STUDENT)
 *  GET  /studentTotal/{reg}            → student total due                 (ADMIN|STUDENT)
 *
 *  ── Students (ADMIN only) ────────────────────────────────────────────────
 *  POST   /students                    → add single attendance record
 *  POST   /students/bulk (multipart)   → bulk CSV/Excel upload
 *  PUT    /students                    → update attendance record
 *  DELETE /students/{reg}/{date}       → delete attendance record
 *
 *  ── Menu (all authenticated) ─────────────────────────────────────────────
 *  GET  /menu                          → full weekly menu
 *  GET  /menu/{day}                    → menu for one day
 *  PUT  /menu/{day}/{mealType}         → update menu item (ADMIN only)
 *
 *  ── Prices (all authenticated) ───────────────────────────────────────────
 *  GET  /prices                        → current meal prices
 *  PUT  /prices/{mealType}             → update a meal price (ADMIN only)
 */
@RestController
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://localhost",
        "https://mess-application-front-end-angular.vercel.app"
})
public class Controller {

    @Autowired MessService        messService;
    @Autowired BulkUploadService  bulkUploadService;
    @Autowired MealPriceRepository  mealPriceRepository;
    @Autowired WeeklyMenuRepository weeklyMenuRepository;

    // ══════════════════════════════════════════════════════════════════════
    //  STUDENT ENDPOINTS
    // ══════════════════════════════════════════════════════════════════════

    @GetMapping("/home")
    public String getHomePage() {
        return messService.getHomePage();
    }

    @GetMapping("/getStudents")
    public List<Student> getStudents(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return messService.getStudents(date);
    }

    @GetMapping("/students/dues")
    public List<StudentDue> getTotalDues(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return messService.getTotalDues(startDate, endDate);
    }

    @GetMapping("/students/{reg}")
    public List<Student> getStudent(@PathVariable String reg) {
        return messService.getStudent(reg);
    }

    @GetMapping("/studentTotal/{reg}")
    public int getStudentTotal(@PathVariable String reg) {
        return messService.getStudentTotal(reg);
    }

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

    @PostMapping(value = "/students/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkUploadResult> bulkUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
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

    // ══════════════════════════════════════════════════════════════════════
    //  MENU ENDPOINTS
    // ══════════════════════════════════════════════════════════════════════

    @GetMapping("/menu")
    public List<WeeklyMenu> getFullMenu() {
        return weeklyMenuRepository.findAll();
    }

    @GetMapping("/menu/{day}")
    public List<WeeklyMenu> getMenuByDay(@PathVariable String day) {
        return weeklyMenuRepository.findByDayOfWeekIgnoreCase(day);
    }

    @PutMapping("/menu/{day}/{mealType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WeeklyMenu> updateMenuItem(
            @PathVariable String day,
            @PathVariable String mealType,
            @RequestBody WeeklyMenu updated) {
        return weeklyMenuRepository
                .findByDayOfWeekIgnoreCaseAndMealTypeIgnoreCase(day, mealType)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setDescription(updated.getDescription());
                    return ResponseEntity.ok(weeklyMenuRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRICE ENDPOINTS
    // ══════════════════════════════════════════════════════════════════════

    @GetMapping("/prices")
    public List<MealPrice> getAllPrices() {
        return mealPriceRepository.findAll();
    }

    @PutMapping("/prices/{mealType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MealPrice> updatePrice(
            @PathVariable String mealType,
            @RequestBody MealPrice updated) {
        return mealPriceRepository.findById(mealType.toUpperCase())
                .map(existing -> {
                    existing.setPriceInr(updated.getPriceInr());
                    return ResponseEntity.ok(mealPriceRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
