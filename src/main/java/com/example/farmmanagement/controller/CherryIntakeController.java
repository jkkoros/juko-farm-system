package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.CherryDelivery;
import com.example.farmmanagement.repository.CherryDeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cherry")
public class CherryIntakeController {

    @Autowired
    private CherryDeliveryRepository deliveryRepo;

    @GetMapping
    public String intakeForm(Model model) {
        model.addAttribute("delivery", new CherryDelivery());
        model.addAttribute("recent", deliveryRepo.findTop20ByOrderByIdDesc());
        return "cherry-intake";
    }

    @PostMapping("/record")
    public String recordDelivery(@ModelAttribute CherryDelivery delivery) {
        // AUTO-FILL farmer_name from surname + middle + last name
        StringBuilder fullName = new StringBuilder();
        if (delivery.getSurname() != null && !delivery.getSurname().isBlank()) {
            fullName.append(delivery.getSurname());
        }
        if (delivery.getMiddleName() != null && !delivery.getMiddleName().isBlank()) {
            fullName.append(" ").append(delivery.getMiddleName());
        }
        if (delivery.getLastName() != null && !delivery.getLastName().isBlank()) {
            fullName.append(" ").append(delivery.getLastName());
        }
        delivery.setFarmerName(fullName.toString().trim());

        // SET DEFAULT cumulative if zero
        if (delivery.getCumulativeKg() == 0) {
            delivery.setCumulativeKg(delivery.getKilosToday());
        }

        deliveryRepo.save(delivery);
        return "redirect:/cherry/receipt/" + delivery.getId();
    }

    @GetMapping("/receipt/{id}")
    public String printReceipt(@PathVariable Long id, Model model) {
        CherryDelivery d = deliveryRepo.findById(id).orElse(null);
        if (d == null) {
            return "redirect:/cherry";
        }
        double cumulative = deliveryRepo.sumKilosTodayByFarmerName(d.getFarmerName());
        model.addAttribute("d", d);
        model.addAttribute("cumulative", cumulative);
        return "cherry-receipt";
    }
}
