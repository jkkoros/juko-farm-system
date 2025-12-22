package com.example.farmmanagement.service;

import com.example.farmmanagement.model.CherryDelivery;
import com.example.farmmanagement.model.Season;
import com.example.farmmanagement.repository.CherryDeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CherryDeliveryService {

    @Autowired
    private CherryDeliveryRepository cherryDeliveryRepository;

    @Autowired
    private SeasonService seasonService;

    // Helper: Get Season by seasonName string (e.g., "2025/2026")
    private Season getSeasonByName(String seasonName) {
        return seasonService.getAllSeasons().stream()
                .filter(s -> s.getSeasonName().equals(seasonName))
                .findFirst()
                .orElse(null);
    }

    public List<Map<String, Object>> getMonthlyTotals(String seasonName) {
        Season season = getSeasonByName(seasonName);
        if (season == null) return new ArrayList<>();

        List<CherryDelivery> deliveries = cherryDeliveryRepository.findBySeasonOrderByIdDesc(season);

        // Use season start year as base (or extract from deliveries)
        int baseYear = deliveries.isEmpty() ? LocalDate.now().getYear() : deliveries.get(0).getDeliveryDate().getYear();

        Map<String, Double> monthlyMap = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyMap.put(String.format("%02d", i), 0.0);
        }

        for (CherryDelivery d : deliveries) {
            String monthKey = String.format("%02d", d.getDeliveryDate().getMonthValue());
            monthlyMap.put(monthKey, monthlyMap.get(monthKey) + d.getKilosToday());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String monthKey = String.format("%02d", i);
            String monthDate = baseYear + "-" + monthKey + "-01"; // Fixed: use baseYear
            result.add(Map.of("month", monthDate, "totalKg", monthlyMap.get(monthKey)));
        }

        return result;
    }

    public List<Map<String, Object>> getWeeklyTotals(String seasonName) {
        Season season = getSeasonByName(seasonName);
        if (season == null) return new ArrayList<>();

        List<CherryDelivery> deliveries = cherryDeliveryRepository.findBySeasonOrderByIdDesc(season);

        Map<Integer, Double> weeklyMap = new HashMap<>();
        for (CherryDelivery d : deliveries) {
            int week = d.getDeliveryDate().getDayOfYear() / 7 + 1; // Approximate week number
            weeklyMap.put(week, weeklyMap.getOrDefault(week, 0.0) + d.getKilosToday());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        weeklyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> result.add(Map.of("week", entry.getKey(), "totalKg", entry.getValue())));

        return result;
    }

    public List<Map<String, Object>> getDailyTotals(String seasonName, LocalDate start, LocalDate end) {
        Season season = getSeasonByName(seasonName);
        if (season == null) return new ArrayList<>();

        List<CherryDelivery> allDeliveries = cherryDeliveryRepository.findBySeasonOrderByIdDesc(season);

        Map<LocalDate, Double> dailyMap = new HashMap<>();
        for (CherryDelivery d : allDeliveries) {
            LocalDate date = d.getDeliveryDate();
            if ((start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end))) {
                dailyMap.put(date, dailyMap.getOrDefault(date, 0.0) + d.getKilosToday());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        dailyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> result.add(Map.of("date", entry.getKey().toString(), "totalKg", entry.getValue())));

        return result;
    }
}
