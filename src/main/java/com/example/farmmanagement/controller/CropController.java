package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.Crop;
import com.example.farmmanagement.service.CropService;
import com.example.farmmanagement.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/crops")
public class CropController {

    @Autowired
    private CropService cropService;

    @Autowired
    private FarmService farmService;

    // 1. List all crops
    @GetMapping
    public String listCrops(Model model) {
        model.addAttribute("crops", cropService.getAllCrops());
        return "crop-list";
    }

    // 2. Show form to add new crop
    @GetMapping("/new")
    public String createCropForm(Model model) {
        model.addAttribute("crop", new Crop());
        model.addAttribute("farms", farmService.getAllFarms());
        model.addAttribute("pageTitle", "Add New Crop");
        return "crop-form";
    }

    // 3. Save crop – no validation
    @PostMapping
    public String saveCrop(@ModelAttribute Crop crop) {
        cropService.saveCrop(crop);
        return "redirect:/crops";
    }

    // 4. Edit crop
    @GetMapping("/edit/{id}")
    public String editCropForm(@PathVariable Long id, Model model) {
        Crop crop = cropService.getCropById(id);
        if (crop == null) {
            return "redirect:/crops";
        }
        model.addAttribute("crop", crop);
        model.addAttribute("farms", farmService.getAllFarms());
        model.addAttribute("pageTitle", "Edit Crop");
        return "crop-form";
    }

    // 5. Delete crop
    @GetMapping("/delete/{id}")
    public String deleteCrop(@PathVariable Long id) {
        cropService.deleteCrop(id);
        return "redirect:/crops";
    }
}