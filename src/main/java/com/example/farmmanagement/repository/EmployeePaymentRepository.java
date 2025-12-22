package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.EmployeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeePaymentRepository extends JpaRepository<EmployeePayment, Long> {
    List<EmployeePayment> findByEmployeeIdOrderByPaymentDateDesc(String employeeId);
}
