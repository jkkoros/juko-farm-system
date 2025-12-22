package com.example.farmmanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "farmers")
public class Farmer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String farmerId;
    private String surname;
    private String middleName;
    private String lastName;
    private String phone;

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

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String buildFullName() {
        StringBuilder name = new StringBuilder();
        if (surname != null && !surname.isBlank()) name.append(surname.trim());
        if (middleName != null && !middleName.isBlank()) name.append(" ").append(middleName.trim());
        if (lastName != null && !lastName.isBlank()) name.append(" ").append(lastName.trim());
        return name.toString().trim();
    }
}