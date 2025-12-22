package com.example.farmmanagement.controller;

import com.example.farmmanagement.service.CherryDeliveryService;
import com.example.farmmanagement.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class ClerkController {

    @Autowired
    private SeasonService seasonService;

    @Autowired
    private CherryDeliveryService cherryDeliveryService;

    @GetMapping("/clerk/tasks")
    public String clerkTasks(Model model) {
        model.addAttribute("selectedSeason", seasonService.getCurrentSeason());
        return "clerk-tasks";
    }

    // API Endpoints for Charts
    @GetMapping("/clerk/api/delivery-stats/monthly")
    @ResponseBody
    public List<Map<String, Object>> getMonthlyStats(@RequestParam String season) {
        return cherryDeliveryService.getMonthlyTotals(season);
    }

    @GetMapping("/clerk/api/delivery-stats/weekly")
    @ResponseBody
    public List<Map<String, Object>> getWeeklyStats(@RequestParam String season) {
        return cherryDeliveryService.getWeeklyTotals(season);
    }

    @GetMapping("/clerk/api/delivery-stats/daily")
    @ResponseBody
    public List<Map<String, Object>> getDailyStats(
            @RequestParam String season,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        LocalDate startDate = start != null && !start.isEmpty() ? LocalDate.parse(start) : null;
        LocalDate endDate = end != null && !end.isEmpty() ? LocalDate.parse(end) : null;

        return cherryDeliveryService.getDailyTotals(season, startDate, endDate);
    }
}
