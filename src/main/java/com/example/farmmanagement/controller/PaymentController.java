package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.*;
import com.example.farmmanagement.repository.*;
import com.example.farmmanagement.service.MpesaService;
import com.example.farmmanagement.service.PdfService;
import com.example.farmmanagement.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/clerk")
public class PaymentController {

    @Autowired
    private FarmerRepository farmerRepo;

    @Autowired
    private CherryDeliveryRepository cherryDeliveryRepo;

    @Autowired
    private ParchmentDeliveryRepository parchmentDeliveryRepo;

    @Autowired
    private FarmInputLoanRepository loanRepo;

    @Autowired
    private LoanRepaymentRepository repaymentRepo;

    @Autowired
    private FarmerPaymentRepository farmerPaymentRepo;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeePaymentRepository employeePaymentRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SeasonService seasonService;

    @Autowired
    private MpesaService mpesaService;

    @Autowired
    private PdfService pdfService;

    /* ====================== FARMER PAYMENT METHODS ====================== */

    @GetMapping("/payment-statement/{farmerId}")
    public String paymentStatement(@PathVariable String farmerId, Model model) {
        populatePaymentStatementModel(farmerId, model);
        return "payment-statement";
    }

    @GetMapping("/payment-statement-pdf/{farmerId}")
    public ResponseEntity<byte[]> paymentStatementPdf(@PathVariable String farmerId) throws Exception {
        Model model = new org.springframework.ui.ConcurrentModel();
        populatePaymentStatementModel(farmerId, model);
        Map<String, Object> variables = new HashMap<>(model.asMap());
        byte[] pdfBytes = pdfService.generatePdf("payment-statement", variables);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Payment-Statement-" + farmerId + ".pdf")
                .build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/payment-report")
    public String allFarmersPaymentReport(Model model) {
        Season currentSeason = seasonService.getCurrentSeason();
        Set<String> cherryFarmers = cherryDeliveryRepo.findBySeasonOrderByIdDesc(currentSeason)
                .stream()
                .filter(d -> d.getFarmerId() != null)
                .map(CherryDelivery::getFarmerId)
                .collect(Collectors.toSet());
        Set<String> parchmentFarmers = parchmentDeliveryRepo.findBySeasonOrderByIdDesc(currentSeason)
                .stream()
                .filter(d -> d.getFarmerId() != null)
                .map(ParchmentDelivery::getFarmerId)
                .collect(Collectors.toSet());
        Set<String> farmerIds = new HashSet<>();
        farmerIds.addAll(cherryFarmers);
        farmerIds.addAll(parchmentFarmers);

        double cherryRatePerKg = 80.0;
        double parchmentRatePerKg = 200.0;

        List<Map<String, Object>> report = new ArrayList<>();
        for (String farmerId : farmerIds) {
            Farmer farmer = farmerRepo.findByFarmerId(farmerId);
            if (farmer == null) continue;

            double cherryKg = cherryDeliveryRepo.findBySeasonOrderByIdDesc(currentSeason)
                    .stream()
                    .filter(d -> farmerId.equals(d.getFarmerId()))
                    .mapToDouble(CherryDelivery::getKilosToday)
                    .sum();
            double parchmentKg = parchmentDeliveryRepo.findBySeasonOrderByIdDesc(currentSeason)
                    .stream()
                    .filter(d -> farmerId.equals(d.getFarmerId()))
                    .mapToDouble(ParchmentDelivery::getKilosToday)
                    .sum();

            double grossCherry = cherryKg * cherryRatePerKg;
            double grossParchment = parchmentKg * parchmentRatePerKg;
            double grossTotal = grossCherry + grossParchment;

            double totalDeducted = repaymentRepo.findByFarmerIdOrderByRepaymentDateDesc(farmerId)
                    .stream()
                    .mapToDouble(LoanRepayment::getAmountRepaid)
                    .sum();

            double netPayable = grossTotal - totalDeducted;

            Map<String, Object> entry = new HashMap<>();
            entry.put("farmer", farmer);
            entry.put("cherryKg", cherryKg);
            entry.put("parchmentKg", parchmentKg);
            entry.put("grossTotal", grossTotal);
            entry.put("totalDeducted", totalDeducted);
            entry.put("netPayable", netPayable);
            report.add(entry);
        }

        report.sort((a, b) -> Double.compare((Double) b.get("netPayable"), (Double) a.get("netPayable")));

        double grandTotalPayable = report.stream()
                .mapToDouble(m -> (Double) m.get("netPayable"))
                .sum();

        model.addAttribute("currentSeason", currentSeason);
        model.addAttribute("report", report);
        model.addAttribute("grandTotalPayable", grandTotalPayable);
        model.addAttribute("cherryRate", cherryRatePerKg);
        model.addAttribute("parchmentRate", parchmentRatePerKg);

        return "all-farmers-payment-report";
    }

    @GetMapping("/record-payment/{farmerId}")
    public String showRecordPaymentForm(@PathVariable String farmerId, Model model) {
        Farmer farmer = farmerRepo.findByFarmerId(farmerId);
        if (farmer == null) {
            model.addAttribute("error", "Farmer not found!");
            return "redirect:/clerk/tasks";
        }
        double netPayable = calculateNetPayable(farmerId);
        FarmerPayment payment = new FarmerPayment();
        payment.setFarmerId(farmerId);
        payment.setFarmerName(farmer.buildFullName());
        payment.setFarmerPhone(farmer.getPhone());
        payment.setPaymentDate(LocalDate.now());

        model.addAttribute("farmer", farmer);
        model.addAttribute("netPayable", netPayable);
        model.addAttribute("payment", payment);
        return "record-payment";
    }

    @PostMapping("/record-payment/{farmerId}")
    public String recordPayment(@PathVariable String farmerId,
                                @ModelAttribute FarmerPayment payment,
                                RedirectAttributes ra) {
        Farmer farmer = farmerRepo.findByFarmerId(farmerId);
        if (farmer == null) {
            ra.addFlashAttribute("error", "Farmer not found!");
            return "redirect:/clerk/tasks";
        }
        payment.setFarmerId(farmerId);
        payment.setFarmerName(farmer.buildFullName());
        payment.setFarmerPhone(farmer.getPhone());
        payment.setSeason(seasonService.getCurrentSeason());

        farmerPaymentRepo.save(payment);

        if (farmer.getPhone() != null && !farmer.getPhone().trim().isEmpty()) {
            try {
                Map<String, Object> mpesaResult = mpesaService.disbursePayment(
                        farmer.getPhone(),
                        payment.getAmountPaid(),
                        "Payment Ref: " + (payment.getReferenceNumber() != null ? payment.getReferenceNumber() : "Manual")
                );
                ra.addFlashAttribute("mpesaSuccess", "Payment recorded and sent via M-Pesa successfully!");
            } catch (Exception e) {
                ra.addFlashAttribute("mpesaError", "Payment recorded but M-Pesa failed: " + e.getMessage());
            }
        } else {
            ra.addFlashAttribute("mpesaWarning", "Payment recorded but no phone number — M-Pesa not sent.");
        }

        ra.addFlashAttribute("successMessage",
                "Payment of KES " + String.format("%,.0f", payment.getAmountPaid()) + " recorded successfully!");
        return "redirect:/clerk/payment-statement/" + farmerId;
    }

    /* ====================== EMPLOYEE PAYMENT WITH OVERDRAFT ====================== */

    @GetMapping("/record-employee-payment/{employeeId}")
    public String showRecordEmployeePaymentForm(@PathVariable String employeeId, Model model) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            model.addAttribute("error", "Employee not found!");
            return "redirect:/employees/all";
        }

        EmployeePayment payment = new EmployeePayment();
        payment.setEmployeeId(employeeId);
        payment.setEmployeeName(employee.buildFullName());
        payment.setEmployeePhone(employee.getPhone());
        payment.setPaymentDate(LocalDate.now());

        double monthlySalary = employee.getMonthlySalary();
        double overdraft = employee.getOverdraftBalance();

        double maxPayable = monthlySalary - overdraft;
        if (maxPayable < 0) maxPayable = 0;

        payment.setGrossSalary(monthlySalary);
        payment.setDeductions(overdraft > 0 ? overdraft : 0);
        payment.setAmountPaid(maxPayable);

        model.addAttribute("employee", employee);
        model.addAttribute("monthlySalary", monthlySalary);
        model.addAttribute("overdraft", overdraft);
        model.addAttribute("maxPayable", maxPayable);
        model.addAttribute("payment", payment);

        return "record-employee-payment";
    }

    @PostMapping("/record-employee-payment/{employeeId}")
    public String recordEmployeePayment(@PathVariable String employeeId,
                                        @ModelAttribute EmployeePayment payment,
                                        RedirectAttributes ra) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            ra.addFlashAttribute("error", "Employee not found!");
            return "redirect:/employees/all";
        }

        double monthlySalary = employee.getMonthlySalary();
        double currentOverdraft = employee.getOverdraftBalance();
        double amountPaid = payment.getAmountPaid();

        double newOverdraft = currentOverdraft;

        if (amountPaid > monthlySalary) {
            newOverdraft = currentOverdraft + (amountPaid - monthlySalary);
            ra.addFlashAttribute("warning", "Overpayment recorded as overdraft of KES " +
                    String.format("%,.0f", amountPaid - monthlySalary));
        } else {
            double recoverable = monthlySalary - amountPaid;
            newOverdraft = currentOverdraft - recoverable;
            if (newOverdraft < 0) newOverdraft = 0;
        }

        employee.setOverdraftBalance(newOverdraft);
        employeeRepository.save(employee);

        payment.setEmployeeId(employeeId);
        payment.setEmployeeName(employee.buildFullName());
        payment.setEmployeePhone(employee.getPhone());
        payment.setSeason(seasonService.getCurrentSeason());
        payment.setGrossSalary(monthlySalary);
        payment.setDeductions(currentOverdraft);

        employeePaymentRepository.save(payment);

        if ("M-Pesa".equals(payment.getPaymentMethod())
                && employee.getPhone() != null && !employee.getPhone().trim().isEmpty()) {
            try {
                mpesaService.disbursePayment(
                        employee.getPhone(),
                        amountPaid,
                        "Salary - Ref: " + (payment.getReferenceNumber() != null ? payment.getReferenceNumber() : "Manual")
                );
                ra.addFlashAttribute("mpesaSuccess", "Salary sent via M-Pesa!");
            } catch (Exception e) {
                ra.addFlashAttribute("mpesaError", "M-Pesa failed: " + e.getMessage());
            }
        }

        ra.addFlashAttribute("successMessage",
                "Salary payment recorded. New overdraft: KES " + String.format("%,.0f", newOverdraft));

        return "redirect:/employees/all";
    }

    @GetMapping("/employee-payment-statement/{employeeId}")
    public String employeePaymentStatement(@PathVariable String employeeId, Model model) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            model.addAttribute("error", "Employee not found");
            return "redirect:/employees/all";
        }

        List<EmployeePayment> payments = employeePaymentRepository
                .findByEmployeeIdOrderByPaymentDateDesc(employeeId);

        double totalPaid = payments.stream().mapToDouble(EmployeePayment::getAmountPaid).sum();

        model.addAttribute("employee", employee);
        model.addAttribute("payments", payments);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("currentSeason", seasonService.getCurrentSeason());

        return "employee-payment-statement";
    }

    /* ====================== PAYROLL REPORT ====================== */

    @GetMapping("/payroll-report")
    public String payrollReport(Model model) {
        Season currentSeason = seasonService.getCurrentSeason();

        List<EmployeePayment> allPayments = employeePaymentRepository.findAll()
                .stream()
                .filter(p -> p.getSeason() != null && p.getSeason().equals(currentSeason))
                .sorted(Comparator.comparing(EmployeePayment::getPaymentDate).reversed())
                .toList();

        Map<Employee, List<EmployeePayment>> paymentsByEmployee = new LinkedHashMap<>();
        double grandTotalGross = 0;
        double grandTotalDeductions = 0;
        double grandTotalPaid = 0;

        for (EmployeePayment payment : allPayments) {
            Employee emp = employeeRepository.findById(payment.getEmployeeId()).orElse(null);
            if (emp == null) continue;

            paymentsByEmployee.computeIfAbsent(emp, k -> new ArrayList<>()).add(payment);

            grandTotalGross += payment.getGrossSalary();
            grandTotalDeductions += payment.getDeductions();
            grandTotalPaid += payment.getAmountPaid();
        }

        List<Map<String, Object>> employeeSummary = new ArrayList<>();
        for (Map.Entry<Employee, List<EmployeePayment>> entry : paymentsByEmployee.entrySet()) {
            Employee emp = entry.getKey();
            List<EmployeePayment> pays = entry.getValue();

            double empGross = pays.stream().mapToDouble(EmployeePayment::getGrossSalary).sum();
            double empDeductions = pays.stream().mapToDouble(EmployeePayment::getDeductions).sum();
            double empPaid = pays.stream().mapToDouble(EmployeePayment::getAmountPaid).sum();
            int paymentCount = pays.size();

            Map<String, Object> summary = new HashMap<>();
            summary.put("employee", emp);
            summary.put("paymentCount", paymentCount);
            summary.put("grossTotal", empGross);
            summary.put("deductionsTotal", empDeductions);
            summary.put("paidTotal", empPaid);
            summary.put("payments", pays);

            employeeSummary.add(summary);
        }

        model.addAttribute("currentSeason", currentSeason);
        model.addAttribute("employeeSummary", employeeSummary);
        model.addAttribute("grandTotalGross", grandTotalGross);
        model.addAttribute("grandTotalDeductions", grandTotalDeductions);
        model.addAttribute("grandTotalPaid", grandTotalPaid);

        return "payroll-report";
    }

    /* ====================== EXPENSES REPORT ====================== */

    @GetMapping("/record-expense")
    public String showRecordExpenseForm(Model model) {
        Expense expense = new Expense();
        expense.setSeason(seasonService.getCurrentSeason());

        model.addAttribute("expense", expense);
        model.addAttribute("currentSeason", seasonService.getCurrentSeason());
        return "record-expense";
    }

    @PostMapping("/record-expense")
    public String recordExpense(@ModelAttribute Expense expense, RedirectAttributes ra) {
        expense.setSeason(seasonService.getCurrentSeason());
        expenseRepository.save(expense);

        ra.addFlashAttribute("successMessage",
                "Expense of KES " + String.format("%,.0f", expense.getAmount()) +
                        " (" + expense.getCategory() + ") recorded!");

        return "redirect:/clerk/expenses-report";
    }

    @GetMapping("/expenses-report")
    public String expensesReport(Model model) {
        Season currentSeason = seasonService.getCurrentSeason();
        List<Expense> expenses = expenseRepository.findBySeasonOrderByExpenseDateDesc(currentSeason);

        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();

        Map<String, Double> categoryTotal = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)));

        model.addAttribute("currentSeason", currentSeason);
        model.addAttribute("expenses", expenses);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("categoryTotal", categoryTotal);

        return "expenses-report";
    }

    /* ====================== PROFIT & LOSS REPORT ====================== */

    @GetMapping("/profit-loss")
    public String profitLossReport(Model model) {
        Season currentSeason = seasonService.getCurrentSeason();

        // Income from deliveries
        double totalCherryKg = cherryDeliveryRepo.findBySeasonOrderByIdDesc(currentSeason)
                .stream()
                .mapToDouble(CherryDelivery::getKilosToday)
                .sum();
        double totalParchmentKg = parchmentDeliveryRepo.findBySeasonOrderByIdDesc(currentSeason)
                .stream()
                .mapToDouble(ParchmentDelivery::getKilosToday)
                .sum();

        double cherryIncome = totalCherryKg * 80.0;
        double parchmentIncome = totalParchmentKg * 200.0;
        double totalIncome = cherryIncome + parchmentIncome;

        // Loans issued
        double totalLoansIssued = loanRepo.getTotalLoansBySeason(currentSeason);

        // Expenses
        double totalExpenses = expenseRepository.findBySeasonOrderByExpenseDateDesc(currentSeason)
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        double netProfit = totalIncome - totalLoansIssued - totalExpenses;

        model.addAttribute("currentSeason", currentSeason);
        model.addAttribute("totalCherryKg", totalCherryKg);
        model.addAttribute("totalParchmentKg", totalParchmentKg);
        model.addAttribute("cherryIncome", cherryIncome);
        model.addAttribute("parchmentIncome", parchmentIncome);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalLoansIssued", totalLoansIssued);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("netProfit", netProfit);

        return "profit-loss-report";
    }

    @GetMapping("/profit-loss/pdf")
    public ResponseEntity<byte[]> profitLossPdf() throws Exception {
        Model model = new org.springframework.ui.ConcurrentModel();
        profitLossReport(model);

        Map<String, Object> variables = new HashMap<>(model.asMap());
        byte[] pdfBytes = pdfService.generatePdf("profit-loss-report", variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Profit-Loss-" + LocalDate.now() + ".pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    /**
     * Payroll Report - PDF Export
     */
    @GetMapping("/payroll-report/pdf")
    public ResponseEntity<byte[]> payrollReportPdf() throws Exception {
        Model model = new org.springframework.ui.ConcurrentModel();
        payrollReport(model); // Reuse the same logic as HTML

        Map<String, Object> variables = new HashMap<>(model.asMap());
        byte[] pdfBytes = pdfService.generatePdf("payroll-report", variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Payroll-Report-" + LocalDate.now() + ".pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    /**
     * Employee Payment Statement - PDF Export
     */
    @GetMapping("/employee-payment-statement-pdf/{employeeId}")
    public ResponseEntity<byte[]> employeePaymentStatementPdf(@PathVariable String employeeId) throws Exception {
        Model model = new org.springframework.ui.ConcurrentModel();
        employeePaymentStatement(employeeId, model); // Reuse HTML logic

        Map<String, Object> variables = new HashMap<>(model.asMap());
        byte[] pdfBytes = pdfService.generatePdf("employee-payment-statement", variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Employee-Payment-Statement-" + employeeId + ".pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    /**
     * All Farmers Payment Report - PDF Export
     */
    @GetMapping("/payment-report-pdf")
    public ResponseEntity<byte[]> allFarmersPaymentReportPdf() throws Exception {
        Model model = new org.springframework.ui.ConcurrentModel();
        allFarmersPaymentReport(model);

        Map<String, Object> variables = new HashMap<>(model.asMap());
        byte[] pdfBytes = pdfService.generatePdf("all-farmers-payment-report", variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("All-Farmers-Payment-Report-" + LocalDate.now() + ".pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /* ====================== CALLBACKS & HELPERS ====================== */

    @PostMapping("/mpesa/result")
    public ResponseEntity<String> mpesaResult(@RequestBody String json) {
        System.out.println("=== M-PESA RESULT CALLBACK ===");
        System.out.println(json);
        return ResponseEntity.ok("Received");
    }

    @PostMapping("/mpesa/timeout")
    public ResponseEntity<String> mpesaTimeout(@RequestBody String json) {
        System.out.println("=== M-PESA TIMEOUT CALLBACK ===");
        System.out.println(json);
        return ResponseEntity.ok("Received");
    }

    private double calculateNetPayable(String farmerId) {
        Season currentSeason = seasonService.getCurrentSeason();
        double totalCherryKg = cherryDeliveryRepo.findBySeasonOrderByIdDesc(currentSeason)
                .stream()
                .filter(d -> d.getFarmerId() != null && d.getFarmerId().equals(farmerId))
                .mapToDouble(CherryDelivery::getKilosToday)
                .sum();
        double totalParchmentKg = parchmentDeliveryRepo.findBySeasonOrderByIdDesc(currentSeason)
                .stream()
                .filter(d -> d.getFarmerId() != null && d.getFarmerId().equals(farmerId))
                .mapToDouble(ParchmentDelivery::getKilosToday)
                .sum();
        double grossTotal = (totalCherryKg * 80.0) + (totalParchmentKg * 200.0);
        double totalDeducted = repaymentRepo.findByFarmerIdOrderByRepaymentDateDesc(farmerId)
                .stream()
                .mapToDouble(LoanRepayment::getAmountRepaid)
                .sum();
        return grossTotal - totalDeducted;
    }

    private void populatePaymentStatementModel(String farmerId, Model model) {
        Farmer farmer = farmerRepo.findByFarmerId(farmerId);
        if (farmer == null) {
            model.addAttribute("error", "Farmer not found!");
            return;
        }
        Season currentSeason = seasonService.getCurrentSeason();

        List<CherryDelivery> cherryDeliveries = cherryDeliveryRepo.findBySeasonOrderByIdDesc(currentSeason)
                .stream()
                .filter(d -> d.getFarmerId() != null && d.getFarmerId().equals(farmerId))
                .toList();
        List<ParchmentDelivery> parchmentDeliveries = parchmentDeliveryRepo.findBySeasonOrderByIdDesc(currentSeason)
                .stream()
                .filter(d -> d.getFarmerId() != null && d.getFarmerId().equals(farmerId))
                .toList();

        double totalCherryKg = cherryDeliveries.stream().mapToDouble(CherryDelivery::getKilosToday).sum();
        double totalParchmentKg = parchmentDeliveries.stream().mapToDouble(ParchmentDelivery::getKilosToday).sum();
        double grossCherry = totalCherryKg * 80.0;
        double grossParchment = totalParchmentKg * 200.0;
        double grossTotal = grossCherry + grossParchment;

        double totalDeducted = repaymentRepo.findByFarmerIdOrderByRepaymentDateDesc(farmerId)
                .stream()
                .mapToDouble(LoanRepayment::getAmountRepaid)
                .sum();

        double netPayable = grossTotal - totalDeducted;

        model.addAttribute("farmer", farmer);
        model.addAttribute("currentSeason", currentSeason);
        model.addAttribute("cherryDeliveries", cherryDeliveries);
        model.addAttribute("parchmentDeliveries", parchmentDeliveries);
        model.addAttribute("totalCherryKg", totalCherryKg);
        model.addAttribute("totalParchmentKg", totalParchmentKg);
        model.addAttribute("grossCherry", grossCherry);
        model.addAttribute("grossParchment", grossParchment);
        model.addAttribute("grossTotal", grossTotal);
        model.addAttribute("totalDeducted", totalDeducted);
        model.addAttribute("netPayable", netPayable);
    }
}