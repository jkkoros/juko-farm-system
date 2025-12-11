package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.CherryDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CherryDeliveryRepository extends JpaRepository<CherryDelivery, Long> {

    // Search by farmer name (case insensitive)
    List<CherryDelivery> findByFarmerNameContainingIgnoreCase(String name);

    // Find all deliveries on a specific date
    List<CherryDelivery> findByDeliveryDate(LocalDate date);

    // Find recent deliveries
    List<CherryDelivery> findTop20ByOrderByIdDesc();

    // CUMULATIVE KILOS FOR ONE FARMER (THIS IS THE KEY!)
    @Query("SELECT COALESCE(SUM(d.kilosToday), 0) FROM CherryDelivery d WHERE d.farmerName = :farmerName")
    double sumKilosTodayByFarmerName(@Param("farmerName") String farmerName);
}
