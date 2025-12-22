package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.LoanRepayment;
import com.example.farmmanagement.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findBySeasonOrderByRepaymentDateDescIdDesc(Season season);

    List<LoanRepayment> findByFarmerIdOrderByRepaymentDateDesc(String farmerId);
}
