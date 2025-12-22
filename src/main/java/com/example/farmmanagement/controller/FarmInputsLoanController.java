package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.FarmInputLoan;
import com.example.farmmanagement.model.Farmer;
import com.example.farmmanagement.model.Season;
import com.example.farmmanagement.repository.FarmInputLoanRepository;
import com.example.farmmanagement.repository.FarmerRepository;
import com.example.farmmanagement.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/clerk/farm-inputs-loan")
public class FarmInputsLoanController {

    @Autowired private FarmInputLoanRepository loanRepo;
    @Autowired private FarmerRepository farmerRepo;
    @Autowired private SeasonService seasonService;

    @GetMapping
    public String loanForm(@RequestParam(required = false) String searchId,
                           @RequestParam(required = false) String seasonName,
                           Model model) {
        Season selectedSeason = (seasonName != null && !seasonName.isEmpty())
                ? seasonService.getSeasonByName(seasonName)
                : seasonService.getCurrentSeason();

        FarmInputLoan loan = new FarmInputLoan();
        loan.setLoanDate(LocalDate.now());

        if (searchId != null && !searchId.trim().isEmpty()) {
            Farmer farmer = farmerRepo.findByFarmerId(searchId.trim());
            if (farmer != null) {
                loan.setFarmerId(farmer.getFarmerId());
                loan.setSurname(farmer.getSurname());
                loan.setMiddleName(farmer.getMiddleName());
                loan.setLastName(farmer.getLastName());
                loan.setFarmerPhone(farmer.getPhone());
                loan.setFarmerName(farmer.buildFullName());
            } else {
                model.addAttribute("notFound", true);
                model.addAttribute("searchedId", searchId.trim());
            }
        }

        List<FarmInputLoan> loans = loanRepo.findBySeasonOrderByIdDesc(selectedSeason);

        model.addAttribute("loans", loans);
        model.addAttribute("loan", loan);
        model.addAttribute("selectedSeason", selectedSeason);
        model.addAttribute("allSeasons", seasonService.getAllSeasons());
        model.addAttribute("totalLoans", loanRepo.getTotalLoansBySeason(selectedSeason));
        model.addAttribute("farmersCount", loanRepo.getUniqueFarmersBySeason(selectedSeason));

        return "farm-inputs-loan";
    }

    @PostMapping("/issue")
    public String issueLoan(@ModelAttribute FarmInputLoan loan, RedirectAttributes ra) {
        loan.setSeason(seasonService.getCurrentSeason());
        Farmer farmer = farmerRepo.findByFarmerId(loan.getFarmerId());
        if (farmer == null) {
            ra.addFlashAttribute("error", "Farmer not found!");
            return "redirect:/clerk/farm-inputs-loan";
        }
        if (loan.getFarmerName() == null || loan.getFarmerName().isBlank()) {
            loan.setFarmerName(farmer.buildFullName());
            loan.setSurname(farmer.getSurname());
            loan.setMiddleName(farmer.getMiddleName());
            loan.setLastName(farmer.getLastName());
            loan.setFarmerPhone(farmer.getPhone());
        }

        // Calculate total cost
        loan.setTotalCost(loan.getQuantity() * loan.getUnitPrice());

        // Update cumulative debt
        double previousDebt = loanRepo.getTotalDebtByFarmerId(loan.getFarmerId());
        loan.setCumulativeDebt(previousDebt + loan.getTotalCost());

        loanRepo.save(loan);

        ra.addFlashAttribute("successMessage", "Farm inputs issued on loan successfully!");
        return "redirect:/clerk/farm-inputs-loan";
    }

    @GetMapping("/receipt/{id}")
    public String printReceipt(@PathVariable Long id, Model model) {
        FarmInputLoan l = loanRepo.findById(id).orElse(null);
        if (l == null) return "redirect:/clerk/farm-inputs-loan";

        model.addAttribute("l", l);
        model.addAttribute("cumulative", loanRepo.getTotalDebtByFarmerId(l.getFarmerId()));
        return "farm-inputs-receipt";
    }
}
