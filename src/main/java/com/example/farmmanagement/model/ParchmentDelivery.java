package com.example.farmmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class ParchmentDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String farmerId;
    private String surname;
    private String middleName;
    private String lastName;
    private String farmerName;
    private String farmerPhone;

    private double kilosToday;
    private double cumulativeKg;

    private LocalDate deliveryDate = LocalDate.now();

    private String notes;

    @ManyToOne
    private Season season;

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

    public double getCumulativeKg() { return cumulativeKg; }
    public void setCumulativeKg(double cumulativeKg) { this.cumulativeKg = cumulativeKg; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }
}
