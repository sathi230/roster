package com.example.roster;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "roster")
public class Roster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empId;
    private LocalDate date;        // Format: YYYY-MM-DD
    private String shift;
    private String status;     // WORKING, WEEK_OFF, PLANNED_LEAVE, etc.
    private String skillset;

    // Constructors
    public Roster() {
    }
    public Roster(String empId, LocalDate date) {
        this.empId = empId;
        this.date = date;
    }
    public Roster(String empId, LocalDate date, String shift, String status, String skillset) {
        this.empId = empId;
        this.date = date;
        this.shift = shift;
        this.status = status;
        this.skillset = skillset;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSkillset() {
        return skillset;
    }

    public void setSkillset(String skillset) {
        this.skillset = skillset;
    }
}
