package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.Farmer;
import com.example.farmmanagement.model.FarmInputLoan;
import com.example.farmmanagement.model.LoanRepayment;
import com.example.farmmanagement.repository.FarmInputLoanRepository;
import com.example.farmmanagement.repository.FarmerRepository;
import com.example.farmmanagement.repository.LoanRepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/clerk")
public class RepaymentController {

    @Autowired
    private FarmerRepository farmerRepo;

    @Autowired
    private LoanRepaymentRepository repaymentRepo;

    @Autowired
    private FarmInputLoanRepository loanRepo;

    /**
     * View repayment history for a specific farmer
     * URL: /clerk/repayments/{farmerId}
     */
    @GetMapping("/repayments/{farmerId}")
    public String repaymentHistory(@PathVariable String farmerId, Model model) {
        Farmer farmer = farmerRepo.findByFarmerId(farmerId);
        if (farmer == null) {
            model.addAttribute("error", "Farmer not found!");
            return "redirect:/clerk/tasks";
        }

        List<LoanRepayment> repayments = repaymentRepo.findByFarmerIdOrderByRepaymentDateDesc(farmerId);
        double currentDebt = loanRepo.getOutstandingDebtByFarmerId(farmerId);

        model.addAttribute("farmer", farmer);
        model.addAttribute("repayments", repayments);
        model.addAttribute("currentDebt", currentDebt);

        return "repayment-history";
    }

    /**
     * Full Loan Statement - loans issued vs repayments
     * URL: /clerk/statement/{farmerId}
     */
    @GetMapping("/statement/{farmerId}")
    public String loanStatement(@PathVariable String farmerId, Model model) {
        Farmer farmer = farmerRepo.findByFarmerId(farmerId);
        if (farmer == null) {
            model.addAttribute("error", "Farmer not found!");
            return "redirect:/clerk/tasks";
        }

        List<FarmInputLoan> loans = loanRepo.findAll()
                .stream()
                .filter(l -> l.getFarmerId().equals(farmerId))
                .sorted((a, b) -> b.getLoanDate().compareTo(a.getLoanDate()))
                .toList();

        List<LoanRepayment> repayments = repaymentRepo.findByFarmerIdOrderByRepaymentDateDesc(farmerId);

        double totalLoans = loans.stream().mapToDouble(FarmInputLoan::getTotalCost).sum();
        double totalRepaid = repayments.stream().mapToDouble(LoanRepayment::getAmountRepaid).sum();
        double outstanding = totalLoans - totalRepaid;

        model.addAttribute("farmer", farmer);
        model.addAttribute("loans", loans);
        model.addAttribute("repayments", repayments);
        model.addAttribute("totalLoans", totalLoans);
        model.addAttribute("totalRepaid", totalRepaid);
        model.addAttribute("outstanding", outstanding);

        return "farmer-loan-statement";
    }

    // Note: Payment Statement moved to PaymentController
    // No payment-related methods here — clean separation!
}