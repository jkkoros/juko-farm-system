package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.CherryDelivery;
import com.example.farmmanagement.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CherryDeliveryRepository extends JpaRepository<CherryDelivery, Long> {

    List<CherryDelivery> findBySeasonOrderByIdDesc(Season season);

    @Query("SELECT COALESCE(SUM(d.kilosToday), 0) FROM CherryDelivery d WHERE d.season = :season")
    double getTotalKilosBySeason(@Param("season") Season season);

    @Query("SELECT COUNT(DISTINCT d.farmerId) FROM CherryDelivery d WHERE d.season = :season")
    long getUniqueFarmersBySeason(@Param("season") Season season);

    @Query("SELECT COALESCE(SUM(d.kilosToday), 0) FROM CherryDelivery d WHERE d.farmerId = :farmerId")
    double getTotalKilosByFarmerId(@Param("farmerId") String farmerId);
}