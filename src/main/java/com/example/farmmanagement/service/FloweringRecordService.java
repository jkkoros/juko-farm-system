package com.example.farmmanagement.service;

import com.example.farmmanagement.model.FloweringRecord;
import com.example.farmmanagement.repository.FloweringRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FloweringRecordService {

    @Autowired
    private FloweringRecordRepository repository;

    public List<FloweringRecord> getAllRecords() {
        return repository.findAllByOrderByFloweringDateDesc();
    }

    public FloweringRecord saveRecord(FloweringRecord record) {
        return repository.save(record);
    }

    public FloweringRecord getRecordById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteRecord(Long id) {
        repository.deleteById(id);
    }
}
