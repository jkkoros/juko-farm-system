package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.FloweringRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FloweringRecordRepository extends JpaRepository<FloweringRecord, Long> {
    List<FloweringRecord> findAllByOrderByFloweringDateDesc();
}
