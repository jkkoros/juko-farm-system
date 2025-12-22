package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.ParchmentDelivery;
import com.example.farmmanagement.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ParchmentDeliveryRepository extends JpaRepository<ParchmentDelivery, Long> {

    List<ParchmentDelivery> findBySeasonOrderByIdDesc(Season season);

    @Query("SELECT COALESCE(SUM(p.kilosToday), 0) FROM ParchmentDelivery p WHERE p.season = ?1")
    double getTotalKilosBySeason(Season season);

    @Query("SELECT COUNT(DISTINCT p.farmerId) FROM ParchmentDelivery p WHERE p.season = ?1")
    long getUniqueFarmersBySeason(Season season);

    @Query("SELECT COALESCE(SUM(p.kilosToday), 0) FROM ParchmentDelivery p WHERE p.farmerId = ?1")
    double getTotalKilosByFarmerId(String farmerId);
}
