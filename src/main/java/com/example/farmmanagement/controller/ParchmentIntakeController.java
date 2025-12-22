package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.*;
import com.example.farmmanagement.repository.*;
import com.example.farmmanagement.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/clerk/parchment-recording")
public class ParchmentIntakeController {

    @Autowired private ParchmentDeliveryRepository deliveryRepo;
    @Autowired private FarmerRepository farmerRepo;
    @Autowired private SeasonService seasonService;
    @Autowired private FarmInputLoanRepository loanRepo;
    @Autowired private LoanRepaymentRepository repaymentRepo;

    // MAIN PAGE
    @GetMapping
    public String intakeForm(@RequestParam(required = false) String searchId,
                             @RequestParam(required = false) String seasonName,
                             Model model) {
        Season selectedSeason = (seasonName != null && !seasonName.isEmpty())
                ? seasonService.getSeasonByName(seasonName)
                : seasonService.getCurrentSeason();

        ParchmentDelivery delivery = new ParchmentDelivery();
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

        List<ParchmentDelivery> deliveries = deliveryRepo.findBySeasonOrderByIdDesc(selectedSeason);

        model.addAttribute("deliveries", deliveries);
        model.addAttribute("delivery", delivery);
        model.addAttribute("selectedSeason", selectedSeason);
        model.addAttribute("allSeasons", seasonService.getAllSeasons());
        model.addAttribute("totalToday", deliveryRepo.getTotalKilosBySeason(selectedSeason));
        model.addAttribute("farmersToday", deliveryRepo.getUniqueFarmersBySeason(selectedSeason));

        return "parchment-intake";
    }

    // RECORD DELIVERY + LOAN REPAYMENT DEDUCTION
    @PostMapping("/record")
    public String recordDelivery(@ModelAttribute ParchmentDelivery delivery, RedirectAttributes ra) {
        delivery.setSeason(seasonService.getCurrentSeason());
        Farmer farmer = farmerRepo.findByFarmerId(delivery.getFarmerId());
        if (farmer == null) {
            ra.addFlashAttribute("error", "Farmer not found!");
            return "redirect:/clerk/parchment-recording";
        }
        if (delivery.getFarmerName() == null || delivery.getFarmerName().isBlank()) {
            delivery.setFarmerName(farmer.buildFullName());
            delivery.setSurname(farmer.getSurname());
            delivery.setMiddleName(farmer.getMiddleName());
            delivery.setLastName(farmer.getLastName());
            delivery.setFarmerPhone(farmer.getPhone());
        }

        double previous = deliveryRepo.getTotalKilosByFarmerId(delivery.getFarmerId());
        delivery.setCumulativeKg(previous + delivery.getKilosToday());
        deliveryRepo.save(delivery);

        // === LOAN REPAYMENT DEDUCTION LOGIC ===
        double outstandingDebt = loanRepo.getOutstandingDebtByFarmerId(delivery.getFarmerId());
        if (outstandingDebt > 0) {
            // Parchment price per kg in KES (adjust to your actual rate)
            double parchmentPricePerKg = 200.0; // Example: KES 200 per kg
            double deliveryValue = delivery.getKilosToday() * parchmentPricePerKg;

            // Deduction percentage (e.g., 20% of delivery value goes to loan repayment)
            double deductionPercent = 0.20;
            double possibleDeduction = deliveryValue * deductionPercent;

            double actualDeduction = Math.min(possibleDeduction, outstandingDebt);

            if (actualDeduction > 0) {
                LoanRepayment repayment = new LoanRepayment();
                repayment.setFarmerId(delivery.getFarmerId());
                repayment.setFarmerName(delivery.getFarmerName());
                repayment.setAmountRepaid(actualDeduction);
                repayment.setPreviousBalance(outstandingDebt);
                repayment.setNewBalance(outstandingDebt - actualDeduction);
                repayment.setSource("PARCHMENT");
                repayment.setSourceId(delivery.getId());
                repayment.setSeason(delivery.getSeason());
                repaymentRepo.save(repayment);

                ra.addFlashAttribute("repaymentMessage",
                        "Loan repayment deducted: KES " + String.format("%,.0f", actualDeduction) +
                        ". New balance: KES " + String.format("%,.0f", (outstandingDebt - actualDeduction)));
            }
        }

        ra.addFlashAttribute("successMessage", "Parchment delivery recorded successfully!");
        return "redirect:/clerk/parchment-recording";
    }

    // PRINT RECEIPT
    @GetMapping("/receipt/{id}")
    public String printReceipt(@PathVariable Long id, Model model) {
        ParchmentDelivery d = deliveryRepo.findById(id).orElse(null);
        if (d == null) return "redirect:/clerk/parchment-recording";

        model.addAttribute("d", d);
        model.addAttribute("cumulative", deliveryRepo.getTotalKilosByFarmerId(d.getFarmerId()));

        // Optional: pass outstanding debt for receipt display
        double outstanding = loanRepo.getOutstandingDebtByFarmerId(d.getFarmerId());
        model.addAttribute("outstandingDebt", outstanding);

        return "parchment-receipt";
    }
}