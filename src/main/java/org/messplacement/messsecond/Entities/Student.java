package org.messplacement.messsecond.Entities;
import jakarta.persistence.*;
import java.time.LocalDate;


@Entity
@Table(name = "student", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"reg", "date"})
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long AUID;


    String reg;
    @ Column(nullable = true)
    LocalDate date;

    boolean breakfast;
    boolean lunch;
    boolean dinner;

    @ Column(nullable = true)
    int total;

    public Student(String reg, LocalDate date, boolean Breakfast, boolean Lunch, boolean Dinner, int total) {
        this.reg = reg;
        this.date = date;
        this.breakfast = Breakfast;
        this.lunch = Lunch;
        this.dinner = Dinner;
        this.total = total;

    }

    public Student() {
        super();
    }

    public long getId() {
        return AUID;
    }
    public void setId(long AUID) { this.AUID = AUID; }
    public void setreg(String  reg) {
        this.reg = reg;
    }
    public String getreg() {
        return reg;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public boolean getBreakfast() {
        return breakfast;
    }
    public void setBreakfast(Boolean breakfast) {
        this.breakfast = breakfast;
    }
    public boolean getLunch() {
        return lunch;
    }
    public void setLunch(Boolean lunch) {
        this.lunch = lunch;
    }
    public boolean getDinner() {
        return dinner;
    }
    public void setDinner(Boolean dinner) {
        this.dinner = dinner;
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
}
