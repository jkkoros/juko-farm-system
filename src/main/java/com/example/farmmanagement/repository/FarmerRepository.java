package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    Farmer findByFarmerId(String farmerId);

    // Search by any part of name or phone
    @Query("SELECT f FROM Farmer f WHERE " +
           "LOWER(f.surname) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(f.middleName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(f.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(f.phone) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Farmer> searchFarmers(@Param("q") String query);
}
