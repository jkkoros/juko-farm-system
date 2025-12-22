package com.example.farmmanagement.service;

import com.example.farmmanagement.model.Season;
import com.example.farmmanagement.repository.SeasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SeasonService {

    @Autowired
    private SeasonRepository seasonRepository;

    public Season getCurrentSeason() {
        LocalDate today = LocalDate.now();
        return seasonRepository.findAll().stream()
                .filter(season ->
                    !today.isBefore(season.getStartDate()) &&
                    !today.isAfter(season.getEndDate()))
                .findFirst()
                .orElse(null);
    }

    public List<Season> getAllSeasons() {
        return seasonRepository.findAllByOrderByStartDateDesc();
    }

    // ADD THIS METHOD
    public Season getSeasonByName(String seasonName) {
        if (seasonName == null || seasonName.isEmpty()) {
            return getCurrentSeason();
        }
        return seasonRepository.findAll().stream()
                .filter(season -> season.getSeasonName().equals(seasonName))
                .findFirst()
                .orElse(getCurrentSeason());
    }
}