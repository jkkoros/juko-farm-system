package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    // For auto-generating next ID
    long count();
}
