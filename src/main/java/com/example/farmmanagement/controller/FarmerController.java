package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.Farmer;
import com.example.farmmanagement.repository.FarmerRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/farmers")
public class FarmerController {

    @Autowired
    private FarmerRepository farmerRepository;

    // Registration form with padded auto-generated ID
    @GetMapping("/register")
    public String registerFarmerForm(Model model) {
        if (!model.containsAttribute("farmer")) {
            long totalFarmers = farmerRepository.count();
            long nextNumber = totalFarmers + 1;
            String nextFarmerId = String.format("%03d", nextNumber); // ← PADDED: 001, 002, 010

            Farmer farmer = new Farmer();
            farmer.setFarmerId(nextFarmerId);
            model.addAttribute("farmer", farmer);
        }
        return "farmers-register";
    }

    // Save farmer
    @PostMapping("/save")
    public String saveFarmer(@ModelAttribute Farmer farmer, RedirectAttributes redirectAttributes) {
        Farmer saved = farmerRepository.save(farmer);
        redirectAttributes.addFlashAttribute("successMessage",
                "Farmer '" + saved.buildFullName() + "' registered with ID: " + saved.getFarmerId() + "!");
        return "redirect:/farmers/register";
    }

    // Search farmers
    @GetMapping("/search")
    public String searchFarmers(@RequestParam(required = false) String q, Model model) {
        if (q != null && !q.trim().isEmpty()) {
            model.addAttribute("results", farmerRepository.searchFarmers(q.trim()));
            model.addAttribute("query", q.trim());
        }
        return "farmers-search";
    }

    // Print farmer card
    @GetMapping("/card/{farmerId}")
    public String printCard(@PathVariable String farmerId, Model model) {
        Farmer farmer = farmerRepository.findByFarmerId(farmerId);
        if (farmer == null) {
            return "redirect:/farmers/search";
        }
        model.addAttribute("farmer", farmer);
        return "farmer-card";
    }

    // List all (optional)
    @GetMapping("/all")
    public String listAll(@RequestParam(required = false) String search, Model model) {
        List<Farmer> farmers;
        if (search != null && !search.trim().isEmpty()) {
            // Search by ID exact match first, then fallback to name/phone
            Farmer byId = farmerRepository.findByFarmerId(search.trim());
            if (byId != null) {
                farmers = List.of(byId);
            } else {
                farmers = farmerRepository.searchFarmers(search.trim()); // your existing method
            }
        } else {
            farmers = farmerRepository.findAll();
        }
        model.addAttribute("farmers", farmers);
        model.addAttribute("search", search);
        return "farmers-list";
    }
}