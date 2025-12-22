package com.example.farmmanagement.service;

import com.example.farmmanagement.model.CoffeeExpense;
import com.example.farmmanagement.repository.CoffeeExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoffeeExpenseService {

    @Autowired
    private CoffeeExpenseRepository repository;

    public List<CoffeeExpense> getAllExpenses() {
        return repository.findAllByOrderByExpenseDateDesc();
    }

    public CoffeeExpense saveExpense(CoffeeExpense expense) {
        return repository.save(expense);
    }

    public CoffeeExpense getExpenseById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteExpense(Long id) {
        repository.deleteById(id);
    }
}
