package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.*;
import com.example.farmmanagement.repository.*;
import com.example.farmmanagement.service.SeasonService;
import com.example.farmmanagement.service.SettingsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/clerk/cherry-intake")
public class CherryIntakeController {

    @Autowired private CherryDeliveryRepository deliveryRepo;
    @Autowired private FarmerRepository farmerRepo;
    @Autowired private SeasonService seasonService;
    @Autowired private CherryTransferRepository transferRepo;
    @Autowired private FarmInputLoanRepository loanRepo;
    @Autowired private LoanRepaymentRepository repaymentRepo;
    @Autowired private SettingsService settingsService;

    // MAIN PAGE
    @GetMapping
    public String intakeForm(@RequestParam(required = false) String searchId,
                             @RequestParam(required = false) String seasonName,
                             Model model) {
        Season selectedSeason = (seasonName != null && !seasonName.isEmpty())
                ? seasonService.getSeasonByName(seasonName)
                : seasonService.getCurrentSeason();

        CherryDelivery delivery = new CherryDelivery();
        delivery.setDeliveryDate(LocalDate.now());

        if (searchId != null && !searchId.trim().isEmpty()) {
            Farmer farmer = farmerRepo.findByFarmerId(searchId.trim());
            if (farmer != null) {
                delivery.setFarmerId(farmer.getFarmerId());
                delivery.setSurname(farmer.getSurname());
                delivery.setMiddleName(farmer.getMiddleName());
                delivery.setLastName(farmer.getLastName());
                delivery.setFarmerPhone(farmer.getPhone());
                delivery.setFarmerName(farmer.buildFullName());
            } else {
                model.addAttribute("notFound", true);
                model.addAttribute("searchedId", searchId.trim());
            }
        }

        List<CherryDelivery> deliveries = deliveryRepo.findBySeasonOrderByIdDesc(selectedSeason);

        model.addAttribute("deliveries", deliveries);
        model.addAttribute("delivery", delivery);
        model.addAttribute("selectedSeason", selectedSeason);
        model.addAttribute("allSeasons", seasonService.getAllSeasons());
        model.addAttribute("totalToday", deliveryRepo.getTotalKilosBySeason(selectedSeason));
        model.addAttribute("farmersToday", deliveryRepo.getUniqueFarmersBySeason(selectedSeason));

        return "cherry-intake";
    }

 // RECORD DELIVERY + ONE-PER-DAY VALIDATION (FIXED)
    @PostMapping("/record")
    public String recordDelivery(@ModelAttribute CherryDelivery delivery, RedirectAttributes ra) {
        delivery.setSeason(seasonService.getCurrentSeason());

        Farmer farmer = farmerRepo.findByFarmerId(delivery.getFarmerId());
        if (farmer == null) {
            ra.addFlashAttribute("error", "Farmer not found!");
            return "redirect:/clerk/cherry-intake";
        }

        if (delivery.getFarmerName() == null || delivery.getFarmerName().isBlank()) {
            delivery.setFarmerName(farmer.buildFullName());
            delivery.setSurname(farmer.getSurname());
            delivery.setMiddleName(farmer.getMiddleName());
            delivery.setLastName(farmer.getLastName());
            delivery.setFarmerPhone(farmer.getPhone());
        }

        // === FIXED: Use the actual delivery date from the form, not today ===
        LocalDate deliveryDate = delivery.getDeliveryDate(); // This comes from the form

        boolean alreadyDeliveredOnThisDate = deliveryRepo.existsByFarmerIdAndDeliveryDateAndSeason(
                delivery.getFarmerId(), deliveryDate, delivery.getSeason());

        if (alreadyDeliveredOnThisDate) {
            ra.addFlashAttribute("error",
                    "Delivery already recorded on " +
                    deliveryDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")) +
                    " for farmer " + delivery.getFarmerId() + " - " + delivery.getFarmerName() +
                    ". Only one delivery per day is allowed.");
            return "redirect:/clerk/cherry-intake";
        }

        // Calculate cumulative (previous total + today's kilos)
        double previousCumulative = deliveryRepo.getTotalKilosByFarmerId(delivery.getFarmerId());
        delivery.setCumulativeKg(previousCumulative + delivery.getKilosToday());

        deliveryRepo.save(delivery);

        // Loan deduction logic (unchanged)
        double outstandingDebt = loanRepo.getOutstandingDebtByFarmerId(delivery.getFarmerId());
        if (outstandingDebt > 0) {
            double cherryPricePerKg = 80.0;
            double deliveryValue = delivery.getKilosToday() * cherryPricePerKg;
            double deductionPercent = settingsService.getLoanDeductionPercent() / 100.0;
            double possibleDeduction = deliveryValue * deductionPercent;
            double actualDeduction = Math.min(possibleDeduction, outstandingDebt);

            if (actualDeduction > 0) {
                LoanRepayment repayment = new LoanRepayment();
                repayment.setFarmerId(delivery.getFarmerId());
                repayment.setFarmerName(delivery.getFarmerName());
                repayment.setAmountRepaid(actualDeduction);
                repayment.setPreviousBalance(outstandingDebt);
                repayment.setNewBalance(outstandingDebt - actualDeduction);
                repayment.setSource("CHERRY");
                repayment.setSourceId(delivery.getId());
                repayment.setSeason(delivery.getSeason());
                repaymentRepo.save(repayment);

                ra.addFlashAttribute("repaymentMessage",
                        "Loan repayment deducted: KES " + String.format("%,.0f", actualDeduction) +
                        ". New balance: KES " + String.format("%,.0f", (outstandingDebt - actualDeduction)));
            }
        }

        ra.addFlashAttribute("successMessage", "Delivery recorded successfully for " +
                deliveryDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")) +
                "! Farmer cumulative: " + String.format("%,.2f", delivery.getCumulativeKg()) + " kg");

        return "redirect:/clerk/cherry-intake";
    }

    // PRINT RECEIPT
    @GetMapping("/receipt/{id}")
    public String printReceipt(@PathVariable Long id, Model model) {
        CherryDelivery d = deliveryRepo.findById(id).orElse(null);
        if (d == null) return "redirect:/clerk/cherry-intake";

        model.addAttribute("d", d);
        model.addAttribute("cumulative", deliveryRepo.getTotalKilosByFarmerId(d.getFarmerId()));

        // Optional: pass outstanding debt or recent repayment for receipt
        double outstanding = loanRepo.getOutstandingDebtByFarmerId(d.getFarmerId());
        model.addAttribute("outstandingDebt", outstanding);

        return "cherry-receipt";
    }

    // TRANSFER PAGE
    @GetMapping("/transfer")
    public String showTransferForm(Model model) {
        if (!model.containsAttribute("transfer")) {
            CherryTransfer transfer = new CherryTransfer();
            transfer.setTransferDate(LocalDate.now());
            model.addAttribute("transfer", transfer);
        }

        List<CherryTransfer> transfers = transferRepo.findAllByOrderByTransferDateDescIdDesc();
        transfers.forEach(t -> {
            Farmer from = farmerRepo.findByFarmerId(t.getFromFarmerId());
            Farmer to = farmerRepo.findByFarmerId(t.getToFarmerId());
            t.setNotes((t.getNotes() != null ? t.getNotes() + " " : "") +
                       "From: " + (from != null ? from.buildFullName() : "Unknown") +
                       " | To: " + (to != null ? to.buildFullName() : "Unknown"));
        });

        model.addAttribute("transfers", transfers);
        return "cherry-transfer";
    }
    
    @GetMapping("/farmer-cumulative")
    @ResponseBody
    public Map<String, Double> getFarmerCumulative(
            @RequestParam String farmerId,
            @RequestParam(required = false) String seasonName) {

        Season season = (seasonName != null && !seasonName.isEmpty())
                ? seasonService.getSeasonByName(seasonName)
                : seasonService.getCurrentSeason();

        double cumulative = deliveryRepo.getTotalKilosByFarmerId(farmerId);
        return Map.of("cumulativeKg", cumulative);
    }
    
 // LOAD EDIT FORM
    @GetMapping("/edit/{id}")
    public String editDeliveryForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        CherryDelivery delivery = deliveryRepo.findById(id).orElse(null);
        if (delivery == null) {
            ra.addFlashAttribute("error", "Delivery record not found!");
            return "redirect:/clerk/cherry-intake";
        }

        // Pre-fill the form with existing data
        model.addAttribute("delivery", delivery);
        model.addAttribute("editMode", true); // To show "Update" button

        // Load common data (same as main page)
        Season selectedSeason = delivery.getSeason();
        List<CherryDelivery> deliveries = deliveryRepo.findBySeasonOrderByIdDesc(selectedSeason);

        model.addAttribute("deliveries", deliveries);
        model.addAttribute("selectedSeason", selectedSeason);
        model.addAttribute("allSeasons", seasonService.getAllSeasons());
        model.addAttribute("totalToday", deliveryRepo.getTotalKilosBySeason(selectedSeason));
        model.addAttribute("farmersToday", deliveryRepo.getUniqueFarmersBySeason(selectedSeason));

        return "cherry-intake"; // Reuse same template
    }

    // UPDATE DELIVERY
    @PostMapping("/update/{id}")
    public String updateDelivery(@PathVariable Long id,
                                 @ModelAttribute CherryDelivery updatedDelivery,
                                 RedirectAttributes ra) {
        CherryDelivery existing = deliveryRepo.findById(id).orElse(null);
        if (existing == null) {
            ra.addFlashAttribute("error", "Delivery record not found!");
            return "redirect:/clerk/cherry-intake";
        }

        // Check for duplicate on the (possibly new) delivery date
        LocalDate newDate = updatedDelivery.getDeliveryDate();
        boolean duplicate = deliveryRepo.existsByFarmerIdAndDeliveryDateAndSeasonAndIdNot(
                existing.getFarmerId(), newDate, existing.getSeason(), id);

        if (duplicate) {
            ra.addFlashAttribute("error",
                    "Another delivery already exists on " +
                    newDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")) +
                    " for this farmer. Only one per day allowed.");
            return "redirect:/clerk/cherry-intake/edit/" + id;
        }

        // Recalculate cumulative: subtract old kilos, add new kilos
        double oldKilos = existing.getKilosToday();
        double newKilos = updatedDelivery.getKilosToday();

        double currentTotalForFarmer = deliveryRepo.getTotalKilosByFarmerId(existing.getFarmerId());
        double newCumulative = currentTotalForFarmer - oldKilos + newKilos;

        // Update fields
        existing.setKilosToday(newKilos);
        existing.setDeliveryDate(newDate);
        existing.setNotes(updatedDelivery.getNotes());
        existing.setCumulativeKg(newCumulative);

        deliveryRepo.save(existing);

        // Loan recalculation (optional – only if price/deduction rules changed)
        // You can extend this later if needed

        ra.addFlashAttribute("successMessage",
                "Delivery updated successfully! New cumulative: " +
                String.format("%,.2f", newCumulative) + " kg");

        return "redirect:/clerk/cherry-intake";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteDelivery(@PathVariable Long id, RedirectAttributes ra) {
        CherryDelivery delivery = deliveryRepo.findById(id).orElse(null);
        if (delivery == null) {
            ra.addFlashAttribute("error", "Delivery record not found!");
            return "redirect:/clerk/cherry-intake";
        }

        String farmerId = delivery.getFarmerId();
        String farmerName = delivery.getFarmerName();
        double deletedKg = delivery.getKilosToday();
        Season season = delivery.getSeason();

        // Step 1: Delete the delivery
        deliveryRepo.delete(delivery);

        // Step 2: Recalculate cumulativeKg for ALL remaining deliveries of this farmer in this season
        List<CherryDelivery> remainingDeliveries = deliveryRepo.findByFarmerIdAndSeasonOrderByDeliveryDateAsc(farmerId, season);

        double runningTotal = 0.0;
        for (CherryDelivery d : remainingDeliveries) {
            runningTotal += d.getKilosToday();
            d.setCumulativeKg(runningTotal);
        }

        // Bulk save (efficient)
        if (!remainingDeliveries.isEmpty()) {
            deliveryRepo.saveAll(remainingDeliveries);
        }

        // Success message
        ra.addFlashAttribute("successMessage",
                "Delivery deleted successfully! Removed " + String.format("%,.2f", deletedKg) +
                " kg from " + farmerId + " - " + farmerName +
                ". Cumulative totals updated for the farmer.");

        return "redirect:/clerk/cherry-intake";
    }

    // PROCESS TRANSFER
    @PostMapping("/transfer")
    public String processTransfer(@ModelAttribute CherryTransfer transfer, RedirectAttributes ra) {
        Farmer from = farmerRepo.findByFarmerId(transfer.getFromFarmerId());
        if (from == null) {
            ra.addFlashAttribute("error", "From Farmer ID not found!");
            ra.addFlashAttribute("transfer", transfer);
            return "redirect:/clerk/cherry-intake/transfer";
        }
        Farmer to = farmerRepo.findByFarmerId(transfer.getToFarmerId());
        if (to == null) {
            ra.addFlashAttribute("error", "To Farmer ID not found!");
            ra.addFlashAttribute("transfer", transfer);
            return "redirect:/clerk/cherry-intake/transfer";
        }

        double available = deliveryRepo.getTotalKilosByFarmerId(transfer.getFromFarmerId());
        if (transfer.getKilosTransferred() > available) {
            ra.addFlashAttribute("error", "Not enough kg! Only " + available + " kg available.");
            ra.addFlashAttribute("transfer", transfer);
            return "redirect:/clerk/cherry-intake/transfer";
        }

        transferRepo.save(transfer);
        ra.addFlashAttribute("successMessage", "Transfer completed successfully!");
        return "redirect:/clerk/cherry-intake/transfer";
    }

    // UNDO TRANSFER
    @PostMapping("/transfer/undo/{id}")
    public String undoTransfer(@PathVariable Long id, RedirectAttributes ra) {
        CherryTransfer t = transferRepo.findById(id).orElse(null);
        if (t == null) {
            ra.addFlashAttribute("error", "Transfer record not found!");
            return "redirect:/clerk/cherry-intake/transfer";
        }
        transferRepo.delete(t);
        ra.addFlashAttribute("successMessage", "Transfer undone successfully!");
        return "redirect:/clerk/cherry-intake/transfer";
    }
}