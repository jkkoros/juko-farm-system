package com.example.farmmanagement.service;

import com.example.farmmanagement.model.QualityRecord;
import com.example.farmmanagement.repository.QualityRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QualityRecordService {

    @Autowired
    private QualityRecordRepository repository;

    public List<QualityRecord> getAllRecords() {
        return repository.findAllByOrderByPulpingDateDesc();
    }

    public QualityRecord saveRecord(QualityRecord record) {
        return repository.save(record);
    }

    public QualityRecord getRecordById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteRecord(Long id) {
        repository.deleteById(id);
    }
}
