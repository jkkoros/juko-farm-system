package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.*;
import com.example.farmmanagement.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CoffeeController {

    @Autowired
    private FarmService farmService;

    @Autowired
    private FloweringRecordService floweringRecordService;

    @Autowired
    private QualityRecordService qualityRecordService;

    @Autowired
    private CoffeeExpenseService coffeeExpenseService;

    @Autowired
    private SeasonService seasonService;

    // ========================
    // MAIN COFFEE HUB PAGES
    // ========================

    @GetMapping("/coffee")
    public String coffeeHub(Model model) {
        model.addAttribute("pageTitle", "Manage Coffee - Juko Coffee Factory");
        return "coffee-hub";
    }

    @GetMapping("/coffee/schedule")
    public String nutritionSchedule(Model model) {
        model.addAttribute("pageTitle", "Coffee Nutrition & Pruning Schedule");
        return "coffee-schedule";
    }

    @GetMapping("/coffee/diseases")
    public String pestsAndDiseases(Model model) {
        model.addAttribute("pageTitle", "Coffee Pests & Diseases");
        return "coffee-diseases";
    }

    @GetMapping("/coffee/yield")
    public String yieldForecasting(Model model) {
        model.addAttribute("pageTitle", "Flowering Records & Yield Forecasting");
        model.addAttribute("farms", farmService.getAllFarms());
        return "coffee-yield";
    }

    @GetMapping("/coffee/quality")
    public String qualityTracking(Model model) {
        model.addAttribute("pageTitle", "Quality Tracking");
        model.addAttribute("farms", farmService.getAllFarms());
        return "coffee-quality";
    }

    @GetMapping("/coffee/expenses")
    public String coffeeExpenses(Model model) {
        model.addAttribute("pageTitle", "Coffee Expenses Management");
        model.addAttribute("farms", farmService.getAllFarms());
        return "coffee-expenses";
    }

    // Cherry Delivery Charts (Analytics - belongs in Coffee Management)
    @GetMapping("/coffee/delivery-charts")
    public String deliveryCharts(Model model) {
        model.addAttribute("pageTitle", "Cherry Delivery Charts");
        model.addAttribute("seasons", seasonService.getAllSeasons());
        model.addAttribute("selectedSeason", seasonService.getCurrentSeason());
        return "coffee-delivery-charts";
    }

    // ==================================
    // API ENDPOINTS - FLOWERING RECORDS
    // ==================================

    @GetMapping("/api/flowering-records")
    @ResponseBody
    public List<FloweringRecord> getAllFloweringRecords() {
        return floweringRecordService.getAllRecords();
    }

    @GetMapping("/api/flowering-records/{id}")
    @ResponseBody
    public ResponseEntity<FloweringRecord> getFloweringRecordById(@PathVariable Long id) {
        FloweringRecord record = floweringRecordService.getRecordById(id);
        return record == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(record);
    }

    @PostMapping("/api/flowering-records")
    @ResponseBody
    public FloweringRecord saveFloweringRecord(@RequestBody FloweringRecord record) {
        return floweringRecordService.saveRecord(record);
    }

    @PutMapping("/api/flowering-records/{id}")
    @ResponseBody
    public ResponseEntity<FloweringRecord> updateFloweringRecord(
            @PathVariable Long id,
            @RequestBody FloweringRecord updated) {
        FloweringRecord existing = floweringRecordService.getRecordById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setType(updated.getType());
        existing.setFloweringDate(updated.getFloweringDate());
        existing.setFarm(updated.getFarm());
        existing.setIntensity(updated.getIntensity());
        existing.setNotes(updated.getNotes());
        existing.setPredictedPeakHarvest(updated.getPredictedPeakHarvest());
        return ResponseEntity.ok(floweringRecordService.saveRecord(existing));
    }

    @DeleteMapping("/api/flowering-records/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteFloweringRecord(@PathVariable Long id) {
        if (floweringRecordService.getRecordById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        floweringRecordService.deleteRecord(id);
        return ResponseEntity.ok().build();
    }

    // ================================
    // API ENDPOINTS - QUALITY RECORDS
    // ================================

    @GetMapping("/api/quality-records")
    @ResponseBody
    public List<QualityRecord> getAllQualityRecords() {
        return qualityRecordService.getAllRecords();
    }

    @GetMapping("/api/quality-records/{id}")
    @ResponseBody
    public ResponseEntity<QualityRecord> getQualityRecordById(@PathVariable Long id) {
        QualityRecord record = qualityRecordService.getRecordById(id);
        return record == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(record);
    }

    @PostMapping("/api/quality-records")
    @ResponseBody
    public QualityRecord saveQualityRecord(@RequestBody QualityRecord record) {
        return qualityRecordService.saveRecord(record);
    }

    @PutMapping("/api/quality-records/{id}")
    @ResponseBody
    public ResponseEntity<QualityRecord> updateQualityRecord(
            @PathVariable Long id,
            @RequestBody QualityRecord updated) {
        QualityRecord existing = qualityRecordService.getRecordById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setFarm(updated.getFarm());
        existing.setPulpingDate(updated.getPulpingDate());
        existing.setParchmentMoisture(updated.getParchmentMoisture());
        existing.setDryingDays(updated.getDryingDays());
        existing.setGrade(updated.getGrade());
        existing.setDefects(updated.getDefects());
        existing.setCuppingScore(updated.getCuppingScore());
        existing.setCuppingNotes(updated.getCuppingNotes());
        existing.setGeneralNotes(updated.getGeneralNotes());
        return ResponseEntity.ok(qualityRecordService.saveRecord(existing));
    }

    @DeleteMapping("/api/quality-records/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteQualityRecord(@PathVariable Long id) {
        if (qualityRecordService.getRecordById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        qualityRecordService.deleteRecord(id);
        return ResponseEntity.ok().build();
    }

    // ===================================
    // API ENDPOINTS - COFFEE EXPENSES
    // ===================================

    @GetMapping("/api/coffee-expenses")
    @ResponseBody
    public List<CoffeeExpense> getAllCoffeeExpenses() {
        return coffeeExpenseService.getAllExpenses();
    }

    @GetMapping("/api/coffee-expenses/{id}")
    @ResponseBody
    public ResponseEntity<CoffeeExpense> getCoffeeExpenseById(@PathVariable Long id) {
        CoffeeExpense expense = coffeeExpenseService.getExpenseById(id);
        return expense == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(expense);
    }

    @PostMapping("/api/coffee-expenses")
    @ResponseBody
    public CoffeeExpense saveCoffeeExpense(@RequestBody CoffeeExpense expense) {
        return coffeeExpenseService.saveExpense(expense);
    }

    @PutMapping("/api/coffee-expenses/{id}")
    @ResponseBody
    public ResponseEntity<CoffeeExpense> updateCoffeeExpense(
            @PathVariable Long id,
            @RequestBody CoffeeExpense updated) {
        CoffeeExpense existing = coffeeExpenseService.getExpenseById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setFarm(updated.getFarm());
        existing.setExpenseDate(updated.getExpenseDate());
        existing.setType(updated.getType());
        existing.setAmount(updated.getAmount());
        existing.setNotes(updated.getNotes());
        return ResponseEntity.ok(coffeeExpenseService.saveExpense(existing));
    }

    @DeleteMapping("/api/coffee-expenses/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCoffeeExpense(@PathVariable Long id) {
        if (coffeeExpenseService.getExpenseById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        coffeeExpenseService.deleteExpense(id);
        return ResponseEntity.ok().build();
    }
}