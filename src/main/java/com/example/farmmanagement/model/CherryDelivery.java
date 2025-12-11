package com.example.farmmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cherry_deliveries")
public class CherryDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String farmerId;           // Farmer ID
    private String surname;            // Surname
    private String middleName;         // Middle Name
    private String lastName;           // Last Name
    private String farmerName;         // Full Name (surname + middle + last)
    private String farmerPhone;        // Phone (optional)
    private double kilosToday;         // Kg delivered today
    private LocalDate deliveryDate = LocalDate.now();  // Date of delivery
    private double cumulativeKg;       // Cumulative total for this farmer

    // Default constructor
    public CherryDelivery() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getFarmerPhone() { return farmerPhone; }
    public void setFarmerPhone(String farmerPhone) { this.farmerPhone = farmerPhone; }

    public double getKilosToday() { return kilosToday; }
    public void setKilosToday(double kilosToday) { this.kilosToday = kilosToday; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public double getCumulativeKg() { return cumulativeKg; }
    public void setCumulativeKg(double cumulativeKg) { this.cumulativeKg = cumulativeKg; }

    // Helper method to build full name
    public String buildFullName() {
        StringBuilder name = new StringBuilder();
        if (surname != null) name.append(surname).append(" ");
        if (middleName != null && !middleName.isBlank()) name.append(middleName).append(" ");
        if (lastName != null) name.append(lastName);
        return name.toString().trim();
    }
}