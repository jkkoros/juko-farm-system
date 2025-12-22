package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.FarmerPayment;
import com.example.farmmanagement.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FarmerPaymentRepository extends JpaRepository<FarmerPayment, Long> {
    List<FarmerPayment> findBySeasonOrderByPaymentDateDesc(Season season);
    List<FarmerPayment> findByFarmerIdOrderByPaymentDateDesc(String farmerId);
}
