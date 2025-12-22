package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.Farmer;
import com.example.farmmanagement.model.FarmInputLoan;
import com.example.farmmanagement.model.Season;
import com.example.farmmanagement.repository.FarmInputLoanRepository;
import com.example.farmmanagement.repository.FarmerRepository;
import com.example.farmmanagement.repository.LoanRepaymentRepository;
import com.example.farmmanagement.service.PdfService;
import com.example.farmmanagement.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private SeasonService seasonService;

    @Autowired
    private FarmInputLoanRepository loanRepo;

    @Autowired
    private LoanRepaymentRepository repaymentRepo;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private PdfService pdfService; // For PDF export

    /**
     * Manager Dashboard - main landing page
     */
    @GetMapping("/dashboard")
    public String managerDashboard(Model model) {
        Season currentSeason = seasonService.getCurrentSeason();

        double totalIssued = loanRepo.getTotalLoansBySeason(currentSeason);

        double totalRepaid = repaymentRepo.findBySeasonOrderByRepaymentDateDescIdDesc(currentSeason)
                .stream()
                .mapToDouble(repayment -> repayment.getAmountRepaid())
                .sum();

        double outstanding = totalIssued - totalRepaid;

        long farmersWithDebt = loanRepo.findBySeasonOrderByIdDesc(currentSeason)
                .stream()
                .map(loan -> loan.getFarmerId())
                .distinct()
                .filter(farmerId -> loanRepo.getOutstandingDebtByFarmerId(farmerId) > 0)
                .count();

        model.addAttribute("selectedSeason", currentSeason);
        model.addAttribute("totalIssued", totalIssued);
        model.addAttribute("totalRepaid", totalRepaid);
        model.addAttribute("outstanding", outstanding);
        model.addAttribute("farmersWithDebt", farmersWithDebt);

        return "manager-dashboard";
    }

    /**
     * Detailed Loan Report (HTML View)
     */
    @GetMapping("/reports/loans")
    public String loanReport(Model model) {
        Season currentSeason = seasonService.getCurrentSeason();

        double totalIssued = loanRepo.getTotalLoansBySeason(currentSeason);

        double totalRepaid = repaymentRepo.findBySeasonOrderByRepaymentDateDescIdDesc(currentSeason)
                .stream()
                .mapToDouble(r -> r.getAmountRepaid())
                .sum();

        double outstanding = totalIssued - totalRepaid;

        long farmersWithDebt = loanRepo.findBySeasonOrderByIdDesc(currentSeason)
                .stream()
                .map(loan -> loan.getFarmerId())
                .distinct()
                .filter(farmerId -> loanRepo.getOutstandingDebtByFarmerId(farmerId) > 0)
                .count();

        // === DETAILED LOAN TABLE: ONE ROW PER LOAN TRANSACTION ===
        List<Map<String, Object>> loanDetails = new ArrayList<>();

        List<FarmInputLoan> loans = loanRepo.findBySeasonOrderByIdDesc(currentSeason);
        for (FarmInputLoan loan : loans) {
            Farmer farmer = farmerRepository.findByFarmerId(loan.getFarmerId());
            if (farmer == null) continue;

            double repaid = repaymentRepo.findByFarmerIdOrderByRepaymentDateDesc(loan.getFarmerId())
                    .stream()
                    .mapToDouble(r -> r.getAmountRepaid())
                    .sum();

            double loanAmount = loan.getTotalCost();
            double outstandingBalance = loanAmount - repaid;

            Map<String, Object> detail = new HashMap<>();
            detail.put("loan", loan);                    // Full loan object (for date, itemName)
            detail.put("farmer", farmer);
            detail.put("loanAmount", loanAmount);
            detail.put("repaid", repaid);
            detail.put("outstanding", outstandingBalance > 0 ? outstandingBalance : 0);

            loanDetails.add(detail);
        }

        model.addAttribute("currentSeason", currentSeason);
        model.addAttribute("totalIssued", totalIssued);
        model.addAttribute("totalRepaid", totalRepaid);
        model.addAttribute("outstanding", outstanding);
        model.addAttribute("farmersWithDebt", farmersWithDebt);
        model.addAttribute("loanDetails", loanDetails);

        return "manager-loan-report";
    }

    /**
     * Loan Report - PDF Export
     */
    @GetMapping("/reports/loans/pdf")
    public ResponseEntity<ByteArrayResource> loanReportPdf() throws Exception {
        // Reuse the same logic as HTML but populate a model manually
        Model model = new org.springframework.ui.ConcurrentModel();
        loanReport(model); // This populates all data into the model

        Map<String, Object> variables = new HashMap<>(model.asMap());
        byte[] pdfBytes = pdfService.generatePdf("manager-loan-report", variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Loan-Report-" + LocalDate.now() + ".pdf")
                .build());
        headers.setContentLength(pdfBytes.length);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    /**
     * Manager Tasks Page
     */
    @GetMapping("/tasks")
    public String managerTasks(Model model) {
        model.addAttribute("selectedSeason", seasonService.getCurrentSeason());
        return "manager-tasks";
    }
}