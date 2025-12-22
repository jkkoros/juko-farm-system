package com.example.farmmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate expenseDate;
    private String category; // Utilities, Maintenance, Supplies, Transport, Salaries, Other
    private String description;
    private double amount;
    private String paidTo; // Supplier name or "Cash"
    private String reference; // Invoice number, receipt, etc.

    @ManyToOne
    private Season season;

    // Constructors
    public Expense() {
        this.expenseDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaidTo() { return paidTo; }
    public void setPaidTo(String paidTo) { this.paidTo = paidTo; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }
}
