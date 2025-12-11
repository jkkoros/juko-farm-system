package com.example.farmmanagement.service;

import com.example.farmmanagement.model.Farm;
import com.example.farmmanagement.model.User;
import com.example.farmmanagement.repository.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    public List<Farm> getAllFarms() {
        return farmRepository.findAll();
    }

    public List<Farm> getFarmsByUser(User user) {
        return farmRepository.findByUser(user);
    }

    public Farm saveFarm(Farm farm) {
        return farmRepository.save(farm);
    }

    public void deleteFarm(Long id) {
        farmRepository.deleteById(id);
    }

    public Farm getFarmById(Long id) {
        return farmRepository.findById(id).orElse(null);
    }
}