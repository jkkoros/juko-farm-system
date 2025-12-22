package com.example.farmmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "quality_records")
public class QualityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    private LocalDate pulpingDate;

    private Double parchmentMoisture; // %

    private Integer dryingDays;

    private String grade; // e.g., AA, AB, PB, C, TT

    private String defects; // e.g., black beans, insect damage

    private Double cuppingScore; // out of 100

    private String cuppingNotes;

    private String generalNotes;

    // Constructors
    public QualityRecord() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }

    public LocalDate getPulpingDate() { return pulpingDate; }
    public void setPulpingDate(LocalDate pulpingDate) { this.pulpingDate = pulpingDate; }

    public Double getParchmentMoisture() { return parchmentMoisture; }
    public void setParchmentMoisture(Double parchmentMoisture) { this.parchmentMoisture = parchmentMoisture; }

    public Integer getDryingDays() { return dryingDays; }
    public void setDryingDays(Integer dryingDays) { this.dryingDays = dryingDays; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getDefects() { return defects; }
    public void setDefects(String defects) { this.defects = defects; }

    public Double getCuppingScore() { return cuppingScore; }
    public void setCuppingScore(Double cuppingScore) { this.cuppingScore = cuppingScore; }

    public String getCuppingNotes() { return cuppingNotes; }
    public void setCuppingNotes(String cuppingNotes) { this.cuppingNotes = cuppingNotes; }

    public String getGeneralNotes() { return generalNotes; }
    public void setGeneralNotes(String generalNotes) { this.generalNotes = generalNotes; }
}
