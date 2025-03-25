package com.gs.ruleengine.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "rosters")
public class Roster extends BaseEntity {
    
    private String employeeId;
    private String employeeName;
    private String department;
    private String shift;
    private LocalDate date;
    private Integer hoursAllocated;
    
    public Roster() {
        super();
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getEmployeeName() {
        return employeeName;
    }
    
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getShift() {
        return shift;
    }
    
    public void setShift(String shift) {
        this.shift = shift;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Integer getHoursAllocated() {
        return hoursAllocated;
    }
    
    public void setHoursAllocated(Integer hoursAllocated) {
        this.hoursAllocated = hoursAllocated;
    }
}
