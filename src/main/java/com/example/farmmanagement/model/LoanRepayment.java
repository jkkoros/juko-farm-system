package com.example.farmmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String farmerId;
    private String farmerName;

    private double amountRepaid;
    private double previousBalance;
    private double newBalance;

    private String source; // "CHERRY" or "PARCHMENT"
    private Long sourceId; // delivery ID

    private LocalDate repaymentDate = LocalDate.now();

    @ManyToOne
    private Season season;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public double getAmountRepaid() { return amountRepaid; }
    public void setAmountRepaid(double amountRepaid) { this.amountRepaid = amountRepaid; }

    public double getPreviousBalance() { return previousBalance; }
    public void setPreviousBalance(double previousBalance) { this.previousBalance = previousBalance; }

    public double getNewBalance() { return newBalance; }
    public void setNewBalance(double newBalance) { this.newBalance = newBalance; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public LocalDate getRepaymentDate() { return repaymentDate; }
    public void setRepaymentDate(LocalDate repaymentDate) { this.repaymentDate = repaymentDate; }

    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }
}
