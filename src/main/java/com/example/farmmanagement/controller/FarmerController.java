package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.Farmer;
import com.example.farmmanagement.repository.FarmerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/farmers")
public class FarmerController {

    @Autowired
    private FarmerRepository farmerRepository;

    // === EXISTING MVC ENDPOINTS ===

    // Registration form with padded auto-generated ID
    @GetMapping("/register")
    public String registerFarmerForm(Model model) {
        if (!model.containsAttribute("farmer")) {
            long totalFarmers = farmerRepository.count();
            long nextNumber = totalFarmers + 1;
            String nextFarmerId = String.format("%03d", nextNumber); // PADDED: 001, 002, 010
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

    // Search farmers (HTML page)
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

    // List all farmers
    @GetMapping("/all")
    public String listAll(@RequestParam(required = false) String search, Model model) {
        List<Farmer> farmers;
        if (search != null && !search.trim().isEmpty()) {
            Farmer byId = farmerRepository.findByFarmerId(search.trim());
            if (byId != null) {
                farmers = List.of(byId);
            } else {
                farmers = farmerRepository.searchFarmers(search.trim());
            }
        } else {
            farmers = farmerRepository.findAll();
        }
        model.addAttribute("farmers", farmers);
        model.addAttribute("search", search);
        return "farmers-list";
    }

    // === NEW: AJAX AUTOCOMPLETE SEARCH ENDPOINT FOR CHERRY INTAKE ===

    /**
     * REST endpoint used by the autocomplete search in cherry intake.
     * Returns JSON list of matching farmers.
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<FarmerSearchResult>> ajaxSearchFarmers(
            @RequestParam("q") String query) {

        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        String searchTerm = query.trim().toLowerCase();

        List<FarmerSearchResult> results = farmerRepository.findAll().stream()
                .filter(farmer -> {
                    String fullName = farmer.buildFullName().toLowerCase();
                    return farmer.getFarmerId().toLowerCase().contains(searchTerm) ||
                           fullName.contains(searchTerm) ||
                           farmer.getSurname().toLowerCase().contains(searchTerm) ||
                           (farmer.getMiddleName() != null && farmer.getMiddleName().toLowerCase().contains(searchTerm)) ||
                           farmer.getLastName().toLowerCase().contains(searchTerm) ||
                           farmer.getPhone().toLowerCase().contains(searchTerm);
                })
                .limit(15) // Prevent too many results
                .map(farmer -> new FarmerSearchResult(
                        farmer.getFarmerId(),
                        farmer.buildFullName(),
                        farmer.getPhone(),
                        farmer.getSurname(),
                        farmer.getMiddleName() != null ? farmer.getMiddleName() : "",
                        farmer.getLastName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    // === DTO for clean JSON response (prevents exposing full entity) ===
    public static class FarmerSearchResult {
        private String farmerId;
        private String fullName;
        private String phone;
        private String surname;
        private String middleName;
        private String lastName;

        public FarmerSearchResult(String farmerId, String fullName, String phone,
                                  String surname, String middleName, String lastName) {
            this.farmerId = farmerId;
            this.fullName = fullName;
            this.phone = phone;
            this.surname = surname;
            this.middleName = middleName;
            this.lastName = lastName;
        }

        // Getters (required for JSON serialization)
        public String getFarmerId() { return farmerId; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getSurname() { return surname; }
        public String getMiddleName() { return middleName; }
        public String getLastName() { return lastName; }
    }
}