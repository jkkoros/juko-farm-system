package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.Farm;
import com.example.farmmanagement.model.User;
import com.example.farmmanagement.repository.UserRepository;
import com.example.farmmanagement.service.ActivityLogService;
import com.example.farmmanagement.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/farms")
public class FarmController {

    @Autowired private FarmService farmService;
    @Autowired private UserRepository userRepository;
    @Autowired private ActivityLogService logService;

    @GetMapping
    public String listFarms(Model model, Authentication auth) {
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Farm> farms;
        if ("ADMIN".equals(currentUser.getRole())) {
            farms = farmService.getAllFarms();
        } else {
            farms = farmService.getFarmsByUser(currentUser);
        }

        model.addAttribute("farms", farms);
        model.addAttribute("currentUser", currentUser);
        return "farm-list";
    }

    @GetMapping("/new")
    public String createFarmForm(Model model) {
        model.addAttribute("farm", new Farm());
        return "farm-form";
    }

    @PostMapping
    public String saveFarm(@ModelAttribute Farm farm, Authentication auth) {
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only FARMER: assign ownership
        if (!"ADMIN".equals(currentUser.getRole())) {
            farm.setUser(currentUser);
        }

        farmService.saveFarm(farm);

        // LOG THE ACTION
        logService.log(username, "FARM_CREATED", 
            "Created farm: " + farm.getName() + " (" + farm.getSizeInAcres() + " acres) in " + farm.getLocation());

        return "redirect:/farms";
    }

    @GetMapping("/edit/{id}")
    public String editFarmForm(@PathVariable Long id, Model model) {
        model.addAttribute("farm", farmService.getFarmById(id));
        return "farm-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteFarm(@PathVariable Long id, Authentication auth) {
        String username = auth.getName();
        Farm farm = farmService.getFarmById(id);
        
        farmService.deleteFarm(id);

        logService.log(username, "FARM_DELETED", "Deleted farm: " + (farm != null ? farm.getName() : "ID " + id));

        return "redirect:/farms";
    }
}