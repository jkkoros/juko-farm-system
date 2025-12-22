package com.example.farmmanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    private String employeeId; // e.g., E001

    private String surname;
    private String middleName;
    private String lastName;
    private String phone;
    private String role;

    // NEW: Monthly salary
    private double monthlySalary; // e.g., 15000.0, 25000.0

    // Constructors
    public Employee() {}

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // NEW
    public double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(double monthlySalary) { this.monthlySalary = monthlySalary; }

    public String buildFullName() {
        return surname + " " + (middleName != null ? middleName + " " : "") + lastName;
    }
    
 // Add this field
    private double overdraftBalance = 0.0; // Positive = employee owes factory (overpaid)

    // Add getter/setter
    public double getOverdraftBalance() {
        return overdraftBalance;
    }

    public void setOverdraftBalance(double overdraftBalance) {
        this.overdraftBalance = overdraftBalance;
    }
}