package com.example.farmmanagement.service;

import com.example.farmmanagement.model.SystemSettings;
import com.example.farmmanagement.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    @Autowired
    private SystemSettingsRepository settingsRepo;

    public double getLoanDeductionPercent() {
        return settingsRepo.findById("loan_deduction_percent")
                .map(s -> Double.parseDouble(s.getValue()))
                .orElse(20.0); // default 20%
    }

    public void setLoanDeductionPercent(double percent) {
        settingsRepo.save(new SystemSettings("loan_deduction_percent", String.valueOf(percent),
                "Percentage of delivery value deducted for loan repayment"));
    }
}
