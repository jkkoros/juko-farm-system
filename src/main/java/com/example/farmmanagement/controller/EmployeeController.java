package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.Employee;
import com.example.farmmanagement.repository.EmployeeRepository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    // Show registration form with auto-generated padded ID (E001, E002...)
    @GetMapping("/register")
    public String registerEmployeeForm(Model model) {
        if (!model.containsAttribute("employee")) {
            long totalEmployees = employeeRepository.count();
            long nextNumber = totalEmployees + 1;
            String nextEmployeeId = "E" + String.format("%03d", nextNumber); // E001, E002...
            Employee employee = new Employee();
            employee.setEmployeeId(nextEmployeeId);
            model.addAttribute("employee", employee);
        }
        return "employees-register"; // → templates/employees-register.html
    }

    // Save employee
    @PostMapping("/save")
    public String saveEmployee(@ModelAttribute Employee employee, RedirectAttributes redirectAttributes) {
        Employee saved = employeeRepository.save(employee);
        redirectAttributes.addFlashAttribute("successMessage",
                "Employee '" + saved.buildFullName() + "' registered with ID: " + saved.getEmployeeId() + "!");
        return "redirect:/employees/register";
    }

    // List all employees
    @GetMapping("/all")
    public String listAllEmployees(@RequestParam(required = false) String search, Model model) {
        List<Employee> employees;
        if (search != null && !search.trim().isEmpty()) {
            // Exact match by Employee ID first
            Employee byId = employeeRepository.findById(search.trim()).orElse(null);
            if (byId != null) {
                employees = List.of(byId);
            } else {
                // Fallback: search by name or role (add method if needed)
                employees = employeeRepository.findAll().stream()
                        .filter(e -> e.buildFullName().toLowerCase().contains(search.toLowerCase()) ||
                                     e.getRole().toLowerCase().contains(search.toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else {
            employees = employeeRepository.findAll();
        }
        model.addAttribute("employees", employees);
        model.addAttribute("search", search);
        return "employees-list";
    }
}
