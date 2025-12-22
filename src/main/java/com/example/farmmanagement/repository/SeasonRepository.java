package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonRepository extends JpaRepository<Season, Long> {

    // Keep this for nice sorting in dropdowns
    List<Season> findAllByOrderByStartDateDesc();
}