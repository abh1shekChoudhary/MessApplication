package org.messplacement.messsecond.Service;

import org.messplacement.messsecond.DTO.StudentDue;
import org.messplacement.messsecond.Dao.MealPriceRepository;
import org.messplacement.messsecond.Dao.MessDao;
import org.messplacement.messsecond.Entities.MealPrice;
import org.messplacement.messsecond.Entities.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessServiceImpl implements MessService {

    @Autowired
    private MessDao messDao;

    @Autowired
    private MealPriceRepository mealPriceRepository;

    // ── Price helpers ──────────────────────────────────────────────────────

    /**
     * Reads current prices from the database.
     * Falls back to default values if the price rows haven't been seeded yet.
     */
    private Map<String, Integer> getPriceMap() {
        List<MealPrice> prices = mealPriceRepository.findAll();
        if (prices.isEmpty()) {
            // Default fallback prices (also seeded by DataInitializer on startup)
            return Map.of("BREAKFAST", 75, "LUNCH", 100, "DINNER", 125);
        }
        return prices.stream()
                .collect(Collectors.toMap(MealPrice::getMealType, MealPrice::getPriceInr));
    }

    // ── Service methods ────────────────────────────────────────────────────

    @Override
    public String getHomePage() {
        return "This is the home page, Service Layer.";
    }

    @Override
    public List<Student> getStudents(LocalDate date) {
        if (date != null) {
            return messDao.findByDate(date);
        }
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
    @Transactional
    public String addStudent(List<Student> students) {
        if (students.isEmpty()) return "No student data provided.";

        Student incomingStudent = students.get(0);
        Optional<Student> existingStudentOpt = messDao.findByRegAndDate(
                incomingStudent.getreg(), incomingStudent.getDate());

        if (existingStudentOpt.isPresent()) {
            Student existingStudent = existingStudentOpt.get();
            if (incomingStudent.getBreakfast()) existingStudent.setBreakfast(true);
            if (incomingStudent.getLunch())     existingStudent.setLunch(true);
            if (incomingStudent.getDinner())    existingStudent.setDinner(true);
            messDao.save(calcTotal(existingStudent));
            return "Student attendance successfully updated.";
        } else {
            messDao.save(calcTotal(incomingStudent));
            return "New student attendance record successfully created.";
        }
    }

    @Override
    public String updateStudent(Student student) {
        messDao.deleteByRegAndDate(student.getreg(), student.getDate());
        messDao.save(calcTotal(student));
        return null;
    }

    @Override
    public String deleteStudent(String reg, LocalDate date) {
        messDao.deleteByRegAndDate(reg, date);
        return null;
    }

    // ── Price calculation (reads from DB) ──────────────────────────────────

    public List<Student> calcTotal(List<Student> students) {
        Map<String, Integer> prices = getPriceMap();
        for (Student s : students) {
            setTotal(s, prices);
        }
        return students;
    }

    public Student calcTotal(Student student) {
        setTotal(student, getPriceMap());
        return student;
    }

    private void setTotal(Student student, Map<String, Integer> prices) {
        int total = 0;
        if (student.getBreakfast()) total += prices.getOrDefault("BREAKFAST", 75);
        if (student.getLunch())     total += prices.getOrDefault("LUNCH",     100);
        if (student.getDinner())    total += prices.getOrDefault("DINNER",    125);
        student.setTotal(total);
        if (student.getDate() == null) student.setDate(LocalDate.now());
    }
}
