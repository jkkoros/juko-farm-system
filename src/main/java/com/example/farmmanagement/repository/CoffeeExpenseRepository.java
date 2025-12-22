package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.CoffeeExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoffeeExpenseRepository extends JpaRepository<CoffeeExpense, Long> {
    List<CoffeeExpense> findAllByOrderByExpenseDateDesc();
}
