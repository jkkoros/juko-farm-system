package com.example.farmmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "flowering_records")
public class FloweringRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FloweringType type;  // MAIN or FLY

    @Column(nullable = false)
    private LocalDate floweringDate;

    @ManyToOne
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    private String intensity;  // Light, Moderate, Heavy, Very Heavy

    private String notes;

    private String predictedPeakHarvest;  // e.g., "November 2026"

    // Constructors
    public FloweringRecord() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FloweringType getType() { return type; }
    public void setType(FloweringType type) { this.type = type; }

    public LocalDate getFloweringDate() { return floweringDate; }
    public void setFloweringDate(LocalDate floweringDate) { this.floweringDate = floweringDate; }

    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }

    public String getIntensity() { return intensity; }
    public void setIntensity(String intensity) { this.intensity = intensity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPredictedPeakHarvest() { return predictedPeakHarvest; }
    public void setPredictedPeakHarvest(String predictedPeakHarvest) { this.predictedPeakHarvest = predictedPeakHarvest; }

    public enum FloweringType {
        MAIN, FLY
    }
}
