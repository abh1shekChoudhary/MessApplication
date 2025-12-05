package org.messplacement.messsecond.DTO;

public class StudentDue {
    private String reg;
    private int totalDue;

    public StudentDue(String reg, long totalDue) {
        this.reg = reg;
        this.totalDue = (int) totalDue;
    }

    public String getReg() {
        return reg;
    }

    public void setReg(String reg) {
        this.reg = reg;
    }

    public int getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(int totalDue) {
        this.totalDue = totalDue;
    }
}