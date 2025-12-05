package org.messplacement.messsecond.Service;

import org.messplacement.messsecond.DTO.StudentDue;
import org.messplacement.messsecond.Dao.MessDao;
import org.messplacement.messsecond.Entities.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- Import Transactional

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MessServiceImpl implements MessService {

    //Calculate total (list of objects)
    public List<Student> calcTotal(List<Student> student) {
        int total = 0;

        for (Student value : student) {
            if (value.getBreakfast()) {
                total += 75;
            }
            if (value.getLunch()) {
                total += 100;
            }
            if (value.getDinner()) {
                total += 125;
            }
            value.setTotal(total);
            total = 0;
            if(value.getDate() == null){value.setDate(LocalDate.now());}

        }
        return student;
    }
// calculate total for single object
    public Student calcTotal(Student student ){
        int total = 0;
        if (student.getBreakfast()) {total += 75;}
        if (student.getLunch() ) {total += 100;}
        if (student.getDinner()) {total += 125;}
        student.setTotal(total);
        if(student.getDate() == null){student.setDate(LocalDate.now());}
        return student;
    }


    @Autowired
    private MessDao messDao;

     public MessServiceImpl() {

    }

    @Override
    public String getHomePage(){
        return "This is the home page, Service Layer.";
    }

    @Override
    public List<Student> getStudents(LocalDate date) {
        // If a date is provided, use the new DAO method
        if (date != null) {
            return messDao.findByDate(date);
        }
        // If no date is provided, return all students (optional fallback)
        return messDao.findAll();
    }
    @Override
    public List<StudentDue> getTotalDues(LocalDate startDate, LocalDate endDate) {
        return messDao.findTotalDuesByDateRange(startDate, endDate);
    }

    @Override
    public List<Student> getStudent(String Reg) {

        return messDao.findByReg(Reg);
    }

    @Override
    public int getStudentTotal(String studentId) {
          return messDao.getStudentTotal(studentId);
    }


    @Override
    @Transactional // This annotation ensures the entire operation is a single database transaction.
    public String addStudent(List<Student> students) {
        // We only expect one student record from the "Add Attendance" form.
        if (students.isEmpty()) {
            return "No student data provided.";
        }
        Student incomingStudent = students.get(0);

        // Find if a record already exists for this student on this date.
        Optional<Student> existingStudentOpt = messDao.findByRegAndDate(incomingStudent.getreg(), incomingStudent.getDate());

        if (existingStudentOpt.isPresent()) {
            // --- RECORD EXISTS: MERGE AND UPDATE ---
            Student existingStudent = existingStudentOpt.get();

            // Merge the meal data. If the incoming value is true, set it to true.
            if (incomingStudent.getBreakfast()) {
                existingStudent.setBreakfast(true);
            }
            if (incomingStudent.getLunch()) {
                existingStudent.setLunch(true);
            }
            if (incomingStudent.getDinner()) {
                existingStudent.setDinner(true);
            }

            // Recalculate the total and save the updated record.
            Student updatedStudent = calcTotal(existingStudent);
            messDao.save(updatedStudent);
            return "Student attendance successfully updated.";

        } else {
            // --- RECORD DOES NOT EXIST: CREATE NEW ---
            // Calculate the total for the new record and save it.
            Student newStudent = calcTotal(incomingStudent);
            messDao.save(newStudent);
            return "New student attendance record successfully created.";
        }
    }

    @Override
    public String updateStudent(Student student) {
         LocalDate date = student.getDate();
         String reg = student.getreg();
         messDao.deleteByRegAndDate(reg,date);

          Student obj = calcTotal(student);
          messDao.save(obj);
        return " Updated Successfully";
    }

    @Override
    public String deleteStudent(String reg, LocalDate date) {

        messDao.deleteByRegAndDate(reg,date);
        return " Deleted Successfully";
    }


}
