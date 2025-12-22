package com.example.farmmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cherry_transfers")
public class CherryTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fromFarmerId;

    @Column(nullable = false)
    private String toFarmerId;

    @Column(nullable = false)
    private double kilosTransferred;

    @Column(nullable = false)
    private LocalDate transferDate = LocalDate.now();

    private String notes;

    // Default constructor
    public CherryTransfer() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromFarmerId() { return fromFarmerId; }
    public void setFromFarmerId(String fromFarmerId) { this.fromFarmerId = fromFarmerId; }

    public String getToFarmerId() { return toFarmerId; }
    public void setToFarmerId(String toFarmerId) { this.toFarmerId = toFarmerId; }

    public double getKilosTransferred() { return kilosTransferred; }
    public void setKilosTransferred(double kilosTransferred) { this.kilosTransferred = kilosTransferred; }

    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}