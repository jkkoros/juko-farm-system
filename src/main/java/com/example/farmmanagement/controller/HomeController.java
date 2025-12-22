package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.Farmer;
import com.example.farmmanagement.repository.FarmerRepository;
import com.example.farmmanagement.service.ActivityLogService;
import com.example.farmmanagement.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ActivityLogService logService;

    @Autowired
    private SeasonService seasonService;

    @Autowired
    private FarmerRepository farmerRepo;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("recentLogs", logService.getRecentLogs());
        model.addAttribute("selectedSeason", seasonService.getCurrentSeason());
        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/farmers/list")
    public String farmersList(Model model) {
        List<Farmer> farmers = farmerRepo.findAll();
        model.addAttribute("farmers", farmers);
        model.addAttribute("selectedSeason", seasonService.getCurrentSeason());
        return "farmers-list";
    }
}