package com.example.farmmanagement.model;

import jakarta.persistence.*;

@Entity
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    private double sizeInAcres;
    private String photo;  // stores filename like "farm1.jpg"

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getSizeInAcres() { return sizeInAcres; }
    public void setSizeInAcres(double sizeInAcres) { this.sizeInAcres = sizeInAcres; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
 // Optional: get full image URL
    public String getPhotoUrl() {
        return photo != null ? "/uploads/" + photo : "/images/no-photo.jpg";
    }
}