package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.Expense;
import com.example.farmmanagement.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findBySeasonOrderByExpenseDateDesc(Season season);
}
