package org.messplacement.messsecond;

import org.messplacement.messsecond.DTO.StudentDue;
import org.messplacement.messsecond.Entities.Student;
import org.messplacement.messsecond.Service.MessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@CrossOrigin(origins = {"http://localhost:4200", "https://mess-application-front-end-angular.vercel.app"})
public class Controller {

    @Autowired
    MessService messService;

    @GetMapping("/home")
    public String getHomePage(){
        return messService.getHomePage();
    }

    @GetMapping("/getStudents")
    public List<Student> getStudents(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return this.messService.getStudents(date);
    }

    // It expects two query parameters: 'startDate' and 'endDate'.
    @GetMapping("/students/dues")
    public List<StudentDue> getTotalDues(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return this.messService.getTotalDues(startDate, endDate);
    }

    @GetMapping("/students/{Reg}")
    public List<Student> getStudent(@PathVariable String  Reg){
        return messService.getStudent(Reg);
    }

    @GetMapping("studentTotal/{reg}")
    public int getStudentTotal(@PathVariable String reg){
        int total =  messService.getStudentTotal(reg);
        return total;
    }

    // Updated addStudent method to handle DataIntegrityViolationException
    @PostMapping("/students")
    public ResponseEntity<String> addStudent(@RequestBody List<Student> students){
        try {
            messService.addStudent(students);
            return new ResponseEntity<>("Student(s) added successfully", HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            System.err.println("Attempted to add duplicate student entry: " + e.getMessage());
            return new ResponseEntity<>("Error: A student entry with this registration number and date already exists.", HttpStatus.CONFLICT);
        } catch (Exception e) {
            System.err.println("Error adding student(s): " + e.getMessage());
            return new ResponseEntity<>("An unexpected error occurred while adding student(s): " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/students")
    public String updateStudent(@RequestBody Student student){
        messService.updateStudent(student);
        return "Student updated";
    }

    @DeleteMapping("/students/{reg}/{date}")
    public String deleteStudent(@PathVariable String reg, @PathVariable String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        messService.deleteStudent(reg, parsedDate);
        return "Student deleted";
    }
}
